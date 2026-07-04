package com.nikolajew.photometadataeditor.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.MediaType
import com.nikolajew.photometadataeditor.domain.model.Photo
import okio.Path.Companion.toPath

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
                Text(
                    text = state.folderPath ?: "Папка не выбрана",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            FilterTabs(
                selected = state.filter,
                onSelect = component::onFilterSelect,
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(state.photos, key = Photo::id) { photo ->
                    PhotoTile(
                        photo = photo,
                        isSelected = photo.id == state.selectedPhotoId,
                        onClick = { component.onPhotoClick(photo.id) },
                    )
                }
            }
        }
        VerticalDivider()
        DetailPanel(
            photo = state.selectedPhoto,
            onToggleProcessed = component::onToggleProcessed,
        )
    }
}

@Composable
private fun FilterTabs(
    selected: LibraryFilter,
    onSelect: (LibraryFilter) -> Unit,
) {
    val filters = LibraryFilter.entries
    TabRow(selectedTabIndex = filters.indexOf(selected)) {
        filters.forEach { filter ->
            Tab(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                text = {
                    Text(
                        text = when (filter) {
                            LibraryFilter.ALL -> "Все"
                            LibraryFilter.UNPROCESSED -> "Необработанные"
                            LibraryFilter.PROCESSED -> "Обработанные"
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun PhotoTile(
    photo: Photo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp),
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (photo.mediaType) {
                MediaType.PHOTO -> AsyncImage(
                    model = photo.path.toPath(),
                    contentDescription = photo.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                MediaType.VIDEO -> Text(
                    text = "видео",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (photo.processed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = photo.fileName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailPanel(
    photo: Photo?,
    onToggleProcessed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(360.dp).fillMaxHeight().padding(16.dp),
        contentAlignment = if (photo == null) Alignment.Center else Alignment.TopStart,
    ) {
        if (photo == null) {
            Text(
                text = "Выберите файл, чтобы редактировать метаданные",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    when (photo.mediaType) {
                        MediaType.PHOTO -> AsyncImage(
                            model = photo.path.toPath(),
                            contentDescription = photo.fileName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                        MediaType.VIDEO -> Text(
                            text = "видео",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = photo.fileName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (photo.mediaType == MediaType.VIDEO) "Видео" else "Фотография",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (photo.processed) "Статус: обработано" else "Статус: не обработано",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (photo.processed) {
                    OutlinedButton(onClick = onToggleProcessed) {
                        Text("Вернуть в необработанные")
                    }
                } else {
                    Button(onClick = onToggleProcessed) {
                        Text("Отметить обработанным")
                    }
                }
            }
        }
    }
}
