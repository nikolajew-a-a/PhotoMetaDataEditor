package com.nikolajew.photometadataeditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nikolajew.photometadataeditor.ui.root.RootComponent
import com.nikolajew.photometadataeditor.ui.root.RootContent
import com.nikolajew.photometadataeditor.ui.theme.AppTheme

@Composable
fun App(root: RootComponent) {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RootContent(component = root, modifier = Modifier.fillMaxSize())
        }
    }
}
