package com.nikolajew.photometadataeditor.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapOverviewContent(component: MapOverviewComponent, modifier: Modifier = Modifier) {
    val photos by component.photosWithLocation.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = "Файлов с геолокацией: ${photos.size}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        val mapState = rememberTileMapState()
        val firstLocation = photos.firstOrNull()?.location

        // центрируем на первой геоточке, пока пользователь сам не подвигал карту
        LaunchedEffect(firstLocation) {
            if (firstLocation != null && !mapState.userInteracted) {
                mapState.centerOn(firstLocation, zoom = 6)
            }
        }

        TileMapView(
            state = mapState,
            markers = photos.mapNotNull { photo ->
                photo.location?.let { MapMarker(point = it, id = photo.id) }
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}
