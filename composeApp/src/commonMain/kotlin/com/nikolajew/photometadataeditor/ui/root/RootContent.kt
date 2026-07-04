package com.nikolajew.photometadataeditor.ui.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.nikolajew.photometadataeditor.ui.library.LibraryContent
import com.nikolajew.photometadataeditor.ui.map.MapOverviewContent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        ) {
            SegmentedButton(
                selected = activeChild is RootComponent.Child.Library,
                onClick = component::onLibraryTabClick,
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("Библиотека")
            }
            SegmentedButton(
                selected = activeChild is RootComponent.Child.Map,
                onClick = component::onMapTabClick,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("Карта")
            }
        }

        Children(stack = component.stack, modifier = Modifier.weight(1f)) {
            when (val child = it.instance) {
                is RootComponent.Child.Library -> LibraryContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )
                is RootComponent.Child.Map -> MapOverviewContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
