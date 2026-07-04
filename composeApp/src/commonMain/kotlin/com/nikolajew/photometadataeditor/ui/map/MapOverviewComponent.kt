package com.nikolajew.photometadataeditor.ui.map

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MapOverviewComponent {

    /** Все файлы библиотеки, у которых есть геолокация. */
    val photosWithLocation: StateFlow<List<Photo>>
}

class DefaultMapOverviewComponent(
    componentContext: ComponentContext,
    observeLibrary: ObserveLibraryUseCase,
) : MapOverviewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _photosWithLocation = MutableStateFlow<List<Photo>>(emptyList())
    override val photosWithLocation: StateFlow<List<Photo>> = _photosWithLocation

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        scope.launch {
            observeLibrary(LibraryFilter.ALL).collect { photos ->
                _photosWithLocation.value = photos.filter { it.location != null }
            }
        }
    }
}
