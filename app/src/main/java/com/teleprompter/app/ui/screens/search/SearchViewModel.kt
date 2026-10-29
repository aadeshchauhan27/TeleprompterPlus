package com.teleprompter.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilter {
    ALL,
    FAVORITES
}

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val results: List<ScriptEntity> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter.ALL)
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    val uiState: StateFlow<SearchUiState> = combine(
        _query.debounce(300).distinctUntilChanged(),
        _filter
    ) { q, f -> Pair(q, f) }
        .flatMapLatest { (q, f) ->
            if (q.isBlank()) {
                flowOf(SearchUiState(query = q, filter = f, results = emptyList(), isSearching = false))
            } else {
                val flow = if (f == SearchFilter.FAVORITES) {
                    scriptRepository.getFavoriteScripts()
                } else {
                    scriptRepository.searchScripts(q)
                }
                combine(flow, flowOf(q), flowOf(f)) { list, currentQ, currentF ->
                    val filtered = if (currentF == SearchFilter.FAVORITES) {
                        list.filter {
                            it.title.contains(currentQ, ignoreCase = true) ||
                                    it.content.contains(currentQ, ignoreCase = true)
                        }
                    } else list
                    SearchUiState(
                        query = currentQ,
                        filter = currentF,
                        results = filtered,
                        isSearching = false
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onFilterChanged(newFilter: SearchFilter) {
        _filter.value = newFilter
    }

    fun toggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            scriptRepository.toggleFavorite(script.id, script.isFavorite)
        }
    }
}
