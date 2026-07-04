package com.nikolajew.photometadataeditor.ui.library

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import com.nikolajew.photometadataeditor.platform.FolderPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LibraryComponent {

    val state: StateFlow<LibraryState>

    fun onOpenFolderClick()
}

data class LibraryState(
    val folderPath: String? = null,
    val photos: List<Photo> = emptyList(),
    val isScanning: Boolean = false,
)

class DefaultLibraryComponent(
    componentContext: ComponentContext,
    private val folderPicker: FolderPicker,
    private val openFolder: OpenFolderUseCase,
    observeLibrary: ObserveLibraryUseCase,
) : LibraryComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(LibraryState())
    override val state: StateFlow<LibraryState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        scope.launch {
            observeLibrary().collect { photos ->
                _state.update { it.copy(photos = photos) }
            }
        }
    }

    override fun onOpenFolderClick() {
        scope.launch {
            val path = folderPicker.pickFolder() ?: return@launch
            _state.update { it.copy(folderPath = path, isScanning = true) }
            try {
                openFolder(path)
            } finally {
                _state.update { it.copy(isScanning = false) }
            }
        }
    }
}
