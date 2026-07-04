package com.nikolajew.photometadataeditor.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nikolajew.photometadataeditor.domain.model.MediaType
import com.nikolajew.photometadataeditor.domain.model.Photo

@Composable
fun LibraryContent(component: LibraryComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()

    Row(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = component::onOpenFolderClick) {
                    Text("Открыть папку")
                }
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.folderPath ?: "Папка не выбрана",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            if (state.photos.isNotEmpty()) {
                Text(
                    text = "Найдено файлов: ${state.photos.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.photos, key = Photo::id) { photo ->
                    PhotoRow(photo)
                }
            }
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

@Composable
private fun PhotoRow(photo: Photo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = photo.fileName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (photo.mediaType == MediaType.VIDEO) "видео" else "фото",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
