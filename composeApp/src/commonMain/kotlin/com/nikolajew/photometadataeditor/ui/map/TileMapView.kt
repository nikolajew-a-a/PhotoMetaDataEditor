package com.nikolajew.photometadataeditor.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlin.math.floor
import kotlin.math.roundToInt

class TileMapState(
    initialCenter: GeoPoint,
    initialZoom: Int,
) {
    var centerLat by mutableStateOf(initialCenter.latitude)
        private set
    var centerLon by mutableStateOf(initialCenter.longitude)
        private set
    var zoom by mutableStateOf(initialZoom)
        private set

    /** true после первого действия пользователя — автоцентрирование больше не вмешивается. */
    var userInteracted by mutableStateOf(false)
        private set

    fun centerOn(point: GeoPoint, zoom: Int? = null) {
        centerLat = point.latitude
        centerLon = point.longitude
        zoom?.let { this.zoom = it.coerceIn(MIN_ZOOM, MAX_ZOOM) }
    }

    fun zoomIn() {
        userInteracted = true
        zoom = (zoom + 1).coerceAtMost(MAX_ZOOM)
    }

    fun zoomOut() {
        userInteracted = true
        zoom = (zoom - 1).coerceAtLeast(MIN_ZOOM)
    }

    fun panBy(dxPx: Float, dyPx: Float) {
        userInteracted = true
        val newX = SlippyMap.lonToTileX(centerLon, zoom) - dxPx / SlippyMap.TILE_SIZE
        val newY = SlippyMap.latToTileY(centerLat, zoom) - dyPx / SlippyMap.TILE_SIZE
        centerLon = SlippyMap.tileXToLon(newX, zoom).coerceIn(-180.0, 180.0)
        centerLat = SlippyMap.tileYToLat(newY, zoom).coerceIn(-85.0, 85.0)
    }

    companion object {
        const val MIN_ZOOM = 2
        const val MAX_ZOOM = 19
    }
}

@Composable
fun rememberTileMapState(
    initialCenter: GeoPoint = GeoPoint(48.0, 15.0),
    initialZoom: Int = 4,
): TileMapState = remember { TileMapState(initialCenter, initialZoom) }

data class MapMarker(
    val point: GeoPoint,
    val id: String = "",
)

@Composable
fun TileMapView(
    state: TileMapState,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    onMapClick: ((GeoPoint) -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .background(Color(0xFFD5E8F0))
            .pointerInput(state) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    state.panBy(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(state, onMapClick) {
                detectTapGestures { offset ->
                    if (onMapClick != null) {
                        val zoom = state.zoom
                        val centerX = SlippyMap.lonToTileX(state.centerLon, zoom)
                        val centerY = SlippyMap.latToTileY(state.centerLat, zoom)
                        val tileX = centerX + (offset.x - size.width / 2.0) / SlippyMap.TILE_SIZE
                        val tileY = centerY + (offset.y - size.height / 2.0) / SlippyMap.TILE_SIZE
                        onMapClick(
                            GeoPoint(
                                latitude = SlippyMap.tileYToLat(tileY, zoom),
                                longitude = SlippyMap.tileXToLon(tileX, zoom),
                            ),
                        )
                    }
                }
            }
            .pointerInput(state) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val delta = event.changes.first().scrollDelta.y
                            if (delta < 0) state.zoomIn() else if (delta > 0) state.zoomOut()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            },
    ) {
        val zoom = state.zoom
        val tileCount = 1 shl zoom
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val centerX = SlippyMap.lonToTileX(state.centerLon, zoom)
        val centerY = SlippyMap.latToTileY(state.centerLat, zoom)
        val tileDp = with(LocalDensity.current) { SlippyMap.TILE_SIZE.toDp() }
        val platformContext = LocalPlatformContext.current

        val firstTileX = floor(centerX - widthPx / 2.0 / SlippyMap.TILE_SIZE).toInt()
        val lastTileX = floor(centerX + widthPx / 2.0 / SlippyMap.TILE_SIZE).toInt()
        val firstTileY = floor(centerY - heightPx / 2.0 / SlippyMap.TILE_SIZE).toInt()
        val lastTileY = floor(centerY + heightPx / 2.0 / SlippyMap.TILE_SIZE).toInt()

        for (tileX in firstTileX..lastTileX) {
            for (tileY in firstTileY..lastTileY) {
                if (tileY < 0 || tileY >= tileCount) continue
                // по долготе карта зациклена
                val wrappedX = ((tileX % tileCount) + tileCount) % tileCount

                val offsetX = ((tileX - centerX) * SlippyMap.TILE_SIZE + widthPx / 2.0).roundToInt()
                val offsetY = ((tileY - centerY) * SlippyMap.TILE_SIZE + heightPx / 2.0).roundToInt()

                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(SlippyMap.tileUrl(zoom, wrappedX, tileY))
                        .httpHeaders(OSM_HEADERS)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .size(tileDp),
                )
            }
        }

        markers.forEach { marker ->
            val markerX = (SlippyMap.lonToTileX(marker.point.longitude, zoom) - centerX) *
                SlippyMap.TILE_SIZE + widthPx / 2.0
            val markerY = (SlippyMap.latToTileY(marker.point.latitude, zoom) - centerY) *
                SlippyMap.TILE_SIZE + heightPx / 2.0
            val markerSizePx = with(LocalDensity.current) { 14.dp.toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (markerX - markerSizePx / 2).roundToInt(),
                            (markerY - markerSizePx / 2).roundToInt(),
                        )
                    }
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, Color.White, CircleShape),
            )
        }

        // обязательная атрибуция OSM
        Surface(
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
        ) {
            Text(
                text = "© OpenStreetMap",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF333333),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

private val OSM_HEADERS = NetworkHeaders.Builder()
    .set("User-Agent", "PhotoMetaDataEditor/0.1 (https://github.com/nikolajew-a-a/PhotoMetaDataEditor)")
    .build()
