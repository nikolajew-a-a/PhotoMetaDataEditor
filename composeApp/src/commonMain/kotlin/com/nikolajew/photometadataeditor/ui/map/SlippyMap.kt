package com.nikolajew.photometadataeditor.ui.map

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

/** Математика тайловой схемы OpenStreetMap (веб-меркатор, slippy map). */
object SlippyMap {

    const val TILE_SIZE = 256

    fun lonToTileX(lon: Double, zoom: Int): Double =
        (lon + 180.0) / 360.0 * (1 shl zoom)

    fun latToTileY(lat: Double, zoom: Int): Double {
        val latRad = lat * PI / 180.0
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)
    }

    fun tileXToLon(x: Double, zoom: Int): Double =
        x / (1 shl zoom) * 360.0 - 180.0

    fun tileYToLat(y: Double, zoom: Int): Double {
        val n = PI - 2.0 * PI * y / (1 shl zoom)
        return 180.0 / PI * atan(sinh(n))
    }

    fun tileUrl(zoom: Int, x: Int, y: Int): String =
        "https://tile.openstreetmap.org/$zoom/$x/$y.png"
}
