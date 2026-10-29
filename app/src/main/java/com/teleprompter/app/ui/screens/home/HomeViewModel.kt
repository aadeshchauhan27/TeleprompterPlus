package com.teleprompter.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedTab: Int = 0, // 0 = All, 1 = Recent, 2 = Favorites
    val allScripts: List<ScriptEntity> = emptyList(),
    val recentScripts: List<ScriptEntity> = emptyList(),
    val favoriteScripts: List<ScriptEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
) {
    val displayedScripts: List<ScriptEntity>
        get() = when (selectedTab) {
            1 -> recentScripts
            2 -> favoriteScripts
            else -> allScripts
        }
}

class HomeViewModel(
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedTab,
        _searchQuery,
        scriptRepository.getAllScripts(),
        scriptRepository.getRecentScripts(5),
        scriptRepository.getFavoriteScripts()
    ) { selectedTab, query, all, recent, favorites ->
        val filteredAll = if (query.isBlank()) all else all.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        val filteredFavorites = if (query.isBlank()) favorites else favorites.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        HomeUiState(
            selectedTab = selectedTab,
            allScripts = filteredAll,
            recentScripts = recent,
            favoriteScripts = filteredFavorites,
            isLoading = false,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            scriptRepository.toggleFavorite(script.id, script.isFavorite)
        }
    }

    fun deleteScript(scriptId: String) {
        viewModelScope.launch {
            scriptRepository.deleteScript(scriptId)
        }
    }

    fun duplicateScript(scriptId: String, onDuplicated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val newId = scriptRepository.duplicateScript(scriptId)
            if (newId != null) {
                onDuplicated(newId)
            }
        }
    }

    fun pasteFromClipboard(clipboardText: String, onCreated: (String) -> Unit) {
        if (clipboardText.isBlank()) return
        viewModelScope.launch {
            val lines = clipboardText.trim().lines()
            val title = lines.firstOrNull()?.take(40)?.trim() ?: "Pasted Script"
            val content = if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else clipboardText.trim()
            val newId = scriptRepository.createScript(
                title = title,
                content = content
            )
            onCreated(newId)
        }
    }
}
