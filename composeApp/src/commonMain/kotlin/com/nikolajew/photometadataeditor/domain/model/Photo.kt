package com.nikolajew.photometadataeditor.domain.model

import kotlinx.datetime.Instant

data class Photo(
    val id: String,
    val path: String,
    val fileName: String,
    val mediaType: MediaType,
    val takenAt: Instant?,
    val location: GeoPoint?,
    val processed: Boolean,
    val thumbnailPath: String? = null,
)
