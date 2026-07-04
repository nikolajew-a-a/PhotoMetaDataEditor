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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.MediaType
import com.nikolajew.photometadataeditor.domain.model.Photo
import kotlin.math.round
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
            isSaving = state.isSaving,
            editError = state.editError,
            onToggleProcessed = component::onToggleProcessed,
            onSaveCaptureDate = component::onSaveCaptureDate,
            onSaveLocation = component::onSaveLocation,
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
    isSaving: Boolean,
    editError: String?,
    onToggleProcessed: () -> Unit,
    onSaveCaptureDate: (String) -> Unit,
    onSaveLocation: (String, String) -> Unit,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
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

                MetadataEditor(
                    photo = photo,
                    isSaving = isSaving,
                    onSaveCaptureDate = onSaveCaptureDate,
                    onSaveLocation = onSaveLocation,
                )

                if (editError != null) {
                    Text(
                        text = editError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

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

@Composable
private fun MetadataEditor(
    photo: Photo,
    isSaving: Boolean,
    onSaveCaptureDate: (String) -> Unit,
    onSaveLocation: (String, String) -> Unit,
) {
    var dateInput by remember(photo.id, photo.takenAt) {
        mutableStateOf(photo.takenAt?.formatForInput() ?: "")
    }
    var latInput by remember(photo.id, photo.location) {
        mutableStateOf(photo.location?.latitude?.round5()?.toString() ?: "")
    }
    var lonInput by remember(photo.id, photo.location) {
        mutableStateOf(photo.location?.longitude?.round5()?.toString() ?: "")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text("Дата съёмки (дд.мм.гггг чч:мм)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onSaveCaptureDate(dateInput) },
            enabled = !isSaving && dateInput.isNotBlank(),
        ) {
            Text("Сохранить дату")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = latInput,
                onValueChange = { latInput = it },
                label = { Text("Широта") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = lonInput,
                onValueChange = { lonInput = it },
                label = { Text("Долгота") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onSaveLocation(latInput, lonInput) },
                enabled = !isSaving && latInput.isNotBlank() && lonInput.isNotBlank(),
            ) {
                Text("Сохранить локацию")
            }
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

/** Дата съёмки хранится как «настенное» время, интерпретированное в UTC. */
private fun Instant.formatForInput(): String {
    val dt = toLocalDateTime(TimeZone.UTC)
    return "${dt.dayOfMonth.pad2()}.${dt.monthNumber.pad2()}.${dt.year} " +
        "${dt.hour.pad2()}:${dt.minute.pad2()}"
}

private fun Int.pad2(): String = toString().padStart(2, '0')

private fun Double.round5(): Double = round(this * 100_000) / 100_000
