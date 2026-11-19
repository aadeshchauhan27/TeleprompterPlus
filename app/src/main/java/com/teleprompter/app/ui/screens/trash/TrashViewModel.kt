package com.teleprompter.app.ui.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    val deletedScripts: StateFlow<List<ScriptEntity>> = scriptRepository.getDeletedScripts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(scriptId: String) {
        viewModelScope.launch { scriptRepository.restoreScript(scriptId) }
    }
}
