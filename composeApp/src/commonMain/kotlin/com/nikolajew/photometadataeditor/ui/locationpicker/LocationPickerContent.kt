package com.nikolajew.photometadataeditor.ui.locationpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nikolajew.photometadataeditor.ui.map.MapMarker
import com.nikolajew.photometadataeditor.ui.map.TileMapView
import com.nikolajew.photometadataeditor.ui.map.rememberTileMapState

@Composable
fun LocationPickerDialog(component: LocationPickerComponent) {
    val picked by component.picked.collectAsState()

    Dialog(
        onDismissRequest = component::onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.size(width = 720.dp, height = 560.dp),
        ) {
            Column {
                Text(
                    text = "Кликните точку на карте",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                val mapState = rememberTileMapState(
                    initialCenter = component.initialLocation
                        ?: com.nikolajew.photometadataeditor.domain.model.GeoPoint(48.0, 15.0),
                    initialZoom = if (component.initialLocation != null) 12 else 4,
                )
                TileMapView(
                    state = mapState,
                    markers = picked?.let { listOf(MapMarker(it)) } ?: emptyList(),
                    onMapClick = component::onMapClick,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                ) {
                    picked?.let {
                        Text(
                            text = "Выбрано: ${it.latitude.round5()}, ${it.longitude.round5()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    TextButton(onClick = component::onCancel) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = component::onConfirm,
                        enabled = picked != null,
                    ) {
                        Text("Сохранить локацию")
                    }
                }
            }
        }
    }
}

private fun Double.round5(): Double = kotlin.math.round(this * 100_000) / 100_000
