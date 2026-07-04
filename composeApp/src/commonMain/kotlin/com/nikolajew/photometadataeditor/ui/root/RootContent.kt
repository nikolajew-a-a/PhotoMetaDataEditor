package com.nikolajew.photometadataeditor.ui.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.nikolajew.photometadataeditor.ui.library.LibraryContent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(stack = component.stack, modifier = modifier) {
        when (val child = it.instance) {
            is RootComponent.Child.Library -> LibraryContent(
                component = child.component,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
