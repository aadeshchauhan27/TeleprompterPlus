package com.teleprompter.app.ui.screens.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorSnapshot(
    val title: String,
    val content: String
)

enum class SaveStatus {
    SAVED,
    SAVING,
    DIRTY
}

data class EditorUiState(
    val scriptId: String? = null,
    val title: String = "",
    val content: String = "",
    val folderId: String? = null,
    val wordCount: Int = 0,
    val estimatedReadingSeconds: Int = 0,
    val saveStatus: SaveStatus = SaveStatus.SAVED,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isInitialized: Boolean = false
)

class EditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val scriptRepository: ScriptRepository
) : ViewModel() {

    private val navScriptId: String? = savedStateHandle["scriptId"]

    private val _uiState = MutableStateFlow(EditorUiState(scriptId = navScriptId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private var isPerformingUndoRedo = false

    private var autoSaveJob: Job? = null
    private var currentEntity: ScriptEntity? = null

    init {
        loadScript()
    }

    private fun loadScript() {
        if (!navScriptId.isNullOrBlank()) {
            viewModelScope.launch {
                val script = scriptRepository.getScriptByIdDirect(navScriptId)
                if (script != null) {
                    currentEntity = script
                    _uiState.update {
                        it.copy(
                            scriptId = script.id,
                            title = script.title,
                            content = script.content,
                            folderId = script.folderId,
                            wordCount = script.wordCount,
                            estimatedReadingSeconds = script.estimatedReadingTime,
                            saveStatus = SaveStatus.SAVED,
                            isInitialized = true
                        )
                    }
                    undoStack.clear()
                    undoStack.add(EditorSnapshot(script.title, script.content))
                    updateUndoRedoAvailability()
                } else {
                    _uiState.update { it.copy(isInitialized = true) }
                }
            }
        } else {
            _uiState.update { it.copy(isInitialized = true) }
            undoStack.add(EditorSnapshot("", ""))
            updateUndoRedoAvailability()
        }
    }

    fun onTitleChanged(newTitle: String) {
        if (_uiState.value.title == newTitle) return
        recordSnapshotForUndo()
        val wordCount = ScriptUtils.calculateWordCount(_uiState.value.content)
        val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
        _uiState.update {
            it.copy(
                title = newTitle,
                wordCount = wordCount,
                estimatedReadingSeconds = readSeconds,
                saveStatus = SaveStatus.DIRTY
            )
        }
        triggerDebouncedAutoSave()
    }

    fun onContentChanged(newContent: String) {
        if (_uiState.value.content == newContent) return
        recordSnapshotForUndo()
        val wordCount = ScriptUtils.calculateWordCount(newContent)
        val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
        _uiState.update {
            it.copy(
                content = newContent,
                wordCount = wordCount,
                estimatedReadingSeconds = readSeconds,
                saveStatus = SaveStatus.DIRTY
            )
        }
        triggerDebouncedAutoSave()
    }

    private fun recordSnapshotForUndo() {
        if (isPerformingUndoRedo) return
        val currentSnapshot = EditorSnapshot(_uiState.value.title, _uiState.value.content)
        if (undoStack.lastOrNull() != currentSnapshot) {
            undoStack.add(currentSnapshot)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            updateUndoRedoAvailability()
        }
    }

    fun undo() {
        if (undoStack.size > 1) {
            isPerformingUndoRedo = true
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            val previous = undoStack.last()
            val wordCount = ScriptUtils.calculateWordCount(previous.content)
            val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
            _uiState.update {
                it.copy(
                    title = previous.title,
                    content = previous.content,
                    wordCount = wordCount,
                    estimatedReadingSeconds = readSeconds,
                    saveStatus = SaveStatus.DIRTY
                )
            }
            isPerformingUndoRedo = false
            updateUndoRedoAvailability()
            triggerDebouncedAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isPerformingUndoRedo = true
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(next)
            val wordCount = ScriptUtils.calculateWordCount(next.content)
            val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
            _uiState.update {
                it.copy(
                    title = next.title,
                    content = next.content,
                    wordCount = wordCount,
                    estimatedReadingSeconds = readSeconds,
                    saveStatus = SaveStatus.DIRTY
                )
            }
            isPerformingUndoRedo = false
            updateUndoRedoAvailability()
            triggerDebouncedAutoSave()
        }
    }

    private fun updateUndoRedoAvailability() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    private fun triggerDebouncedAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(600)
            saveScriptInternal()
        }
    }

    fun saveExplicitly(onSaved: () -> Unit = {}) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            saveScriptInternal()
            onSaved()
        }
    }

    private suspend fun saveScriptInternal() {
        val currentState = _uiState.value
        val title = currentState.title.ifBlank { "Untitled Script" }
        val content = currentState.content

        _uiState.update { it.copy(saveStatus = SaveStatus.SAVING) }

        val currentId = currentState.scriptId
        if (currentId.isNullOrBlank()) {
            val newId = scriptRepository.createScript(
                title = title,
                content = content,
                folderId = currentState.folderId
            )
            val newEntity = scriptRepository.getScriptByIdDirect(newId)
            currentEntity = newEntity
            _uiState.update {
                it.copy(
                    scriptId = newId,
                    saveStatus = SaveStatus.SAVED
                )
            }
        } else {
            val existing = currentEntity ?: scriptRepository.getScriptByIdDirect(currentId)
            val updated = existing?.copy(
                title = title,
                content = content,
                folderId = currentState.folderId,
                updatedAt = System.currentTimeMillis()
            ) ?: ScriptEntity(
                id = currentId,
                title = title,
                content = content,
                folderId = currentState.folderId,
                updatedAt = System.currentTimeMillis()
            )
            scriptRepository.updateScript(updated)
            currentEntity = updated
            _uiState.update { it.copy(saveStatus = SaveStatus.SAVED) }
        }
    }
}
