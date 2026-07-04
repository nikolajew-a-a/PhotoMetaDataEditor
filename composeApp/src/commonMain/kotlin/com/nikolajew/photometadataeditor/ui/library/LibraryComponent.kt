package com.nikolajew.photometadataeditor.ui.library

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.usecase.ObserveLibraryUseCase
import com.nikolajew.photometadataeditor.domain.usecase.OpenFolderUseCase
import com.nikolajew.photometadataeditor.domain.usecase.SetProcessedUseCase
import com.nikolajew.photometadataeditor.domain.usecase.UpdateCaptureDateUseCase
import com.nikolajew.photometadataeditor.domain.usecase.UpdateLocationUseCase
import com.nikolajew.photometadataeditor.platform.FolderPicker
import com.nikolajew.photometadataeditor.ui.locationpicker.DefaultLocationPickerComponent
import com.nikolajew.photometadataeditor.ui.locationpicker.LocationPickerComponent
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable

interface LibraryComponent {

    val state: StateFlow<LibraryState>

    fun onOpenFolderClick()

    fun onFilterSelect(filter: LibraryFilter)

    fun onPhotoClick(id: String)

    fun onToggleProcessed()

    /** Ожидает формат "дд.мм.гггг чч:мм[:сс]". */
    fun onSaveCaptureDate(input: String)

    fun onSaveLocation(latitudeInput: String, longitudeInput: String)

    /** Диалог выбора точки на карте (открыт, когда слот не пуст). */
    val locationPicker: Value<ChildSlot<*, LocationPickerComponent>>

    fun onPickLocationClick()
}

data class LibraryState(
    val folderPath: String? = null,
    val filter: LibraryFilter = LibraryFilter.ALL,
    val photos: List<Photo> = emptyList(),
    val selectedPhotoId: String? = null,
    val isScanning: Boolean = false,
    val isSaving: Boolean = false,
    val editError: String? = null,
) {
    val selectedPhoto: Photo? get() = photos.find { it.id == selectedPhotoId }
}

class DefaultLibraryComponent(
    componentContext: ComponentContext,
    private val folderPicker: FolderPicker,
    private val openFolder: OpenFolderUseCase,
    private val setProcessed: SetProcessedUseCase,
    private val updateCaptureDate: UpdateCaptureDateUseCase,
    private val updateLocation: UpdateLocationUseCase,
    observeLibrary: ObserveLibraryUseCase,
) : LibraryComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val filterFlow = MutableStateFlow(LibraryFilter.ALL)

    private val _state = MutableStateFlow(LibraryState())
    override val state: StateFlow<LibraryState> = _state

    private val pickerNavigation = SlotNavigation<LocationPickerConfig>()

    override val locationPicker: Value<ChildSlot<*, LocationPickerComponent>> =
        childSlot(
            source = pickerNavigation,
            serializer = LocationPickerConfig.serializer(),
            handleBackButton = true,
        ) { config, childContext ->
            DefaultLocationPickerComponent(
                componentContext = childContext,
                initialLocation = config.toGeoPoint(),
                onResult = { point ->
                    pickerNavigation.dismiss()
                    saveEdit { updateLocation(config.photoId, point) }
                },
                onDismiss = pickerNavigation::dismiss,
            )
        }

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

    override fun onSaveCaptureDate(input: String) {
        val photo = _state.value.selectedPhoto ?: return
        val takenAt = parseDateInput(input)
        if (takenAt == null) {
            _state.update { it.copy(editError = "Неверный формат даты, нужен: дд.мм.гггг чч:мм") }
            return
        }
        saveEdit { updateCaptureDate(photo.id, takenAt) }
    }

    override fun onSaveLocation(latitudeInput: String, longitudeInput: String) {
        val photo = _state.value.selectedPhoto ?: return
        val latitude = latitudeInput.replace(',', '.').trim().toDoubleOrNull()
        val longitude = longitudeInput.replace(',', '.').trim().toDoubleOrNull()
        if (latitude == null || longitude == null) {
            _state.update { it.copy(editError = "Координаты — десятичные числа, например 55.75583") }
            return
        }
        saveEdit { updateLocation(photo.id, GeoPoint(latitude, longitude)) }
    }

    override fun onPickLocationClick() {
        val photo = _state.value.selectedPhoto ?: return
        pickerNavigation.activate(
            LocationPickerConfig(
                photoId = photo.id,
                latitude = photo.location?.latitude,
                longitude = photo.location?.longitude,
            ),
        )
    }

    private fun saveEdit(block: suspend () -> Unit) {
        scope.launch {
            _state.update { it.copy(isSaving = true, editError = null) }
            try {
                block()
            } catch (e: Exception) {
                _state.update { it.copy(editError = e.message ?: "Не удалось записать метаданные") }
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    @Serializable
    private data class LocationPickerConfig(
        val photoId: String,
        val latitude: Double?,
        val longitude: Double?,
    ) {
        fun toGeoPoint(): GeoPoint? =
            if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null
    }

    private companion object {
        val DATE_INPUT_REGEX =
            Regex("""^(\d{2})\.(\d{2})\.(\d{4})\s+(\d{2}):(\d{2})(?::(\d{2}))?$""")

        fun parseDateInput(input: String): Instant? {
            val match = DATE_INPUT_REGEX.find(input.trim()) ?: return null
            val (day, month, year, hour, minute) =
                match.destructured.toList().take(5).map(String::toInt)
            val second = match.groupValues[6].takeIf(String::isNotEmpty)?.toInt() ?: 0

            return runCatching {
                LocalDateTime(year, month, day, hour, minute, second)
                    .toInstant(TimeZone.UTC)
            }.getOrNull()
        }
    }
}
