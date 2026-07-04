package com.nikolajew.photometadataeditor.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibraryContent(component: LibraryComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()

    Row(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
        ) {
            Text(
                text = "Библиотека",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = state.folderPath ?: "Папка не выбрана",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        VerticalDivider()
        Box(
            modifier = Modifier.width(360.dp).fillMaxHeight().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Выберите файл, чтобы редактировать метаданные",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
