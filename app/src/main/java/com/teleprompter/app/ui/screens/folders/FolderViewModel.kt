package com.teleprompter.app.ui.screens.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.local.model.FolderWithCount
import com.teleprompter.app.data.repository.FolderRepository
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderUiState(
    val foldersWithCount: List<FolderWithCount> = emptyList(),
    val selectedFolderId: String? = null,
    val selectedFolderName: String = "",
    val folderScripts: List<ScriptEntity> = emptyList(),
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val targetFolderForRename: FolderWithCount? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FolderViewModel(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    private val _selectedFolderId = MutableStateFlow<String?>(savedStateHandle["folderId"])
    private val _showCreateDialog = MutableStateFlow(false)
    private val _targetFolderForRename = MutableStateFlow<FolderWithCount?>(null)

    val uiState: StateFlow<FolderUiState> = combine(
        folderRepository.getFoldersWithCount(),
        _selectedFolderId,
        _showCreateDialog,
        _targetFolderForRename
    ) { folders, selectedId, showCreate, targetRename ->
        val selectedFolder = folders.find { it.id == selectedId }
        Pair(
            FolderUiState(
                foldersWithCount = folders,
                selectedFolderId = selectedId,
                selectedFolderName = selectedFolder?.name ?: "",
                showCreateDialog = showCreate,
                showRenameDialog = targetRename != null,
                targetFolderForRename = targetRename
            ),
            selectedId
        )
    }.flatMapLatest { (state, selectedId) ->
        if (selectedId != null) {
            combine(
                flowOf(state),
                scriptRepository.getScriptsInFolder(selectedId)
            ) { currentState, scripts ->
                currentState.copy(folderScripts = scripts)
            }
        } else {
            flowOf(state)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FolderUiState(isLoading = true)
    )

    fun selectFolder(folderId: String?) {
        _selectedFolderId.value = folderId
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun createFolder(name: String, colorHex: String = "#3B82F6") {
        if (name.isBlank()) return
        viewModelScope.launch {
            folderRepository.createFolder(name.trim(), colorHex)
            dismissCreateDialog()
        }
    }

    fun openRenameDialog(folder: FolderWithCount) {
        _targetFolderForRename.value = folder
    }

    fun dismissRenameDialog() {
        _targetFolderForRename.value = null
    }

    fun renameFolder(id: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            folderRepository.renameFolder(id, newName.trim())
            dismissRenameDialog()
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            if (_selectedFolderId.value == id) {
                _selectedFolderId.value = null
            }
            folderRepository.deleteFolder(id)
        }
    }
}
