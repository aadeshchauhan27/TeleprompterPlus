package com.teleprompter.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.local.entity.FolderEntity
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.FolderRepository
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScriptDetailUiState(
    val script: ScriptEntity? = null,
    val folderName: String? = null,
    val availableFolders: List<FolderEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showFolderDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

class ScriptDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val scriptRepository: ScriptRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    val scriptId: String = checkNotNull(savedStateHandle["scriptId"])

    private val _dialogState = MutableStateFlow(Pair(false, false)) // (showFolderDialog, showDeleteDialog)

    val uiState: StateFlow<ScriptDetailUiState> = combine(
        scriptRepository.getScriptById(scriptId),
        folderRepository.getAllFolders(),
        _dialogState
    ) { script, folders, dialogs ->
        val folder = script?.folderId?.let { fid -> folders.find { it.id == fid } }
        ScriptDetailUiState(
            script = script,
            folderName = folder?.name,
            availableFolders = folders,
            isLoading = false,
            showFolderDialog = dialogs.first,
            showDeleteDialog = dialogs.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScriptDetailUiState()
    )

    fun toggleFavorite() {
        val current = uiState.value.script ?: return
        viewModelScope.launch {
            scriptRepository.toggleFavorite(current.id, current.isFavorite)
        }
    }

    fun duplicateScript(onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val newId = scriptRepository.duplicateScript(scriptId)
            if (newId != null) {
                onDuplicated(newId)
            }
        }
    }

    fun openFolderDialog() {
        _dialogState.value = _dialogState.value.copy(first = true)
    }

    fun dismissFolderDialog() {
        _dialogState.value = _dialogState.value.copy(first = false)
    }

    fun moveToFolder(folderId: String?) {
        viewModelScope.launch {
            scriptRepository.moveToFolder(scriptId, folderId)
            dismissFolderDialog()
        }
    }

    fun openDeleteDialog() {
        _dialogState.value = _dialogState.value.copy(second = true)
    }

    fun dismissDeleteDialog() {
        _dialogState.value = _dialogState.value.copy(second = false)
    }

    fun deleteScript(onDeleted: () -> Unit) {
        viewModelScope.launch {
            scriptRepository.deleteScript(scriptId)
            dismissDeleteDialog()
            onDeleted()
        }
    }

    fun getExportText(): String {
        val s = uiState.value.script ?: return ""
        return "${s.title}\n\n${s.content}"
    }
}
