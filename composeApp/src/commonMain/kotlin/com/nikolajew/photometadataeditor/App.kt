package com.nikolajew.photometadataeditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.nikolajew.photometadataeditor.ui.root.RootComponent
import com.nikolajew.photometadataeditor.ui.root.RootContent
import com.nikolajew.photometadataeditor.ui.theme.AppTheme

@Composable
fun App(root: RootComponent) {
    // сетевой fetcher нужен для загрузки карт-тайлов OSM
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RootContent(component = root, modifier = Modifier.fillMaxSize())
        }
    }
}
