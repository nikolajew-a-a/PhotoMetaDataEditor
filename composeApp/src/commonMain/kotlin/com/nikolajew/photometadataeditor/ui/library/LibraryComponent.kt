package com.nikolajew.photometadataeditor.ui.library

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import com.nikolajew.photometadataeditor.domain.usecase.SetProcessedUseCase
import com.nikolajew.photometadataeditor.platform.FolderPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LibraryComponent {

    val state: StateFlow<LibraryState>

    fun onOpenFolderClick()

    fun onFilterSelect(filter: LibraryFilter)

    fun onPhotoClick(id: String)

    fun onToggleProcessed()
}

data class LibraryState(
    val folderPath: String? = null,
    val filter: LibraryFilter = LibraryFilter.ALL,
    val photos: List<Photo> = emptyList(),
    val selectedPhotoId: String? = null,
    val isScanning: Boolean = false,
) {
    val selectedPhoto: Photo? get() = photos.find { it.id == selectedPhotoId }
}

class DefaultLibraryComponent(
    componentContext: ComponentContext,
    private val folderPicker: FolderPicker,
    private val openFolder: OpenFolderUseCase,
    private val setProcessed: SetProcessedUseCase,
    observeLibrary: ObserveLibraryUseCase,
) : LibraryComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val filterFlow = MutableStateFlow(LibraryFilter.ALL)

    private val _state = MutableStateFlow(LibraryState())
    override val state: StateFlow<LibraryState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        scope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            filterFlow
                .flatMapLatest { filter ->
                    observeLibrary(filter).also {
                        _state.update { state -> state.copy(filter = filter) }
                    }
                }
                .collect { photos ->
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

    override fun onFilterSelect(filter: LibraryFilter) {
        filterFlow.value = filter
    }

    override fun onPhotoClick(id: String) {
        _state.update {
            it.copy(selectedPhotoId = if (it.selectedPhotoId == id) null else id)
        }
    }

    override fun onToggleProcessed() {
        val photo = _state.value.selectedPhoto ?: return
        scope.launch {
            setProcessed(listOf(photo.id), !photo.processed)
        }
    }
}
