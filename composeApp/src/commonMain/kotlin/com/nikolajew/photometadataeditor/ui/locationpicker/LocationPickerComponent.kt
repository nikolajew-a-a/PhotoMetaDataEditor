package com.nikolajew.photometadataeditor.ui.locationpicker

import com.arkivanov.decompose.ComponentContext
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LocationPickerComponent {

    /** Стартовая позиция карты — текущая локация фото, если есть. */
    val initialLocation: GeoPoint?

    val picked: StateFlow<GeoPoint?>

    fun onMapClick(point: GeoPoint)

    fun onConfirm()

    fun onCancel()
}

class DefaultLocationPickerComponent(
    componentContext: ComponentContext,
    override val initialLocation: GeoPoint?,
    private val onResult: (GeoPoint) -> Unit,
    private val onDismiss: () -> Unit,
) : LocationPickerComponent, ComponentContext by componentContext {

    private val _picked = MutableStateFlow(initialLocation)
    override val picked: StateFlow<GeoPoint?> = _picked

    override fun onMapClick(point: GeoPoint) {
        _picked.value = point
    }

    override fun onConfirm() {
        _picked.value?.let(onResult)
    }

    override fun onCancel() {
        onDismiss()
    }
}
