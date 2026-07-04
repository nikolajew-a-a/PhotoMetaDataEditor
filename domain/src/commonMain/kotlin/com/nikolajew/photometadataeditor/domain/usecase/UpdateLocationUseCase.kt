package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import com.nikolajew.photometadataeditor.domain.repository.MetadataRepository

class UpdateLocationUseCase(
    private val repository: MetadataRepository,
) {

    suspend operator fun invoke(photoId: String, location: GeoPoint) {
        require(location.latitude in -90.0..90.0) { "Широта вне диапазона ±90" }
        require(location.longitude in -180.0..180.0) { "Долгота вне диапазона ±180" }
        repository.updateLocation(photoId, location)
    }
}
