# Архитектура PhotoMetaDataEditor

Clean Architecture + MVI-подобные компоненты Decompose + Koin. Весь код, кроме платформенных реализаций, живёт в `commonMain` и переиспользуется в будущей Android-версии.

## Слои

Зависимости направлены строго вниз: `ui → domain ← data`. Слой `domain` — чистый Kotlin без зависимостей на Compose, БД и файловую систему.

### ui (presentation)

- **Decompose-компоненты** — держат состояние экрана (`StateFlow<State>`), обрабатывают действия пользователя, вызывают use case'ы. Заменяют ViewModel. Не знают про Compose.
- **Compose-экраны** — тонкие функции, которые рисуют `State` компонента и пробрасывают события обратно. Ничего не решают сами.

### domain

- **Модели**: `Photo` (id, путь, тип фото/видео, дата съёмки, `GeoPoint?`, processed, путь к миниатюре), `GeoPoint` (lat, lon), `MediaType`, `LibraryFilter` (all / processed / unprocessed).
- **Интерфейсы репозиториев**: `PhotoLibraryRepository`, `MetadataRepository`, `SettingsRepository`.
- **Use case'ы** (по одному классу на операцию):
  - `OpenFolderUseCase`, `RescanFolderUseCase`
  - `ObserveLibraryUseCase(filter): Flow<List<Photo>>`
  - `SetProcessedUseCase(ids, processed)`
  - `UpdateCaptureDateUseCase(id, dateTime)`
  - `UpdateLocationUseCase(id, geoPoint)`
  - `ShiftCaptureDatesUseCase(ids, offset)` — батч-сдвиг даты
  - `SetLocationForAllUseCase(ids, geoPoint)` — батч-локация

### data

- `PhotoLibraryRepositoryImpl` — объединяет сканер файловой системы, SQLite-индекс и генератор миниатюр; отдаёт реактивный `Flow` библиотеки.
- `MetadataRepositoryImpl` — оркестрирует запись метаданных: бэкап → запись через `ExifToolClient` → контрольное чтение → обновление индекса.
- **SQLDelight** — таблица `photo`: `path`, `content_hash`, `media_type`, `taken_at`, `lat`, `lon`, `processed`, `thumb_path`, `indexed_at`. Хэш содержимого позволяет пережить переименование файла.
- `ExifToolClient` — интерфейс в data-слое; desktop-реализация в `desktopMain`.

### Платформенный слой (`desktopMain`, позже `androidMain`)

Реализации через Koin-модуль платформы (без `expect/actual`-функций, просто интерфейсы + байндинги):

| Интерфейс | Desktop-реализация | Android (план) |
|---|---|---|
| `ExifToolClient` | ProcessBuilder, `-stay_open` | exifinterface + mp4parser |
| `FolderPicker` | AWT FileDialog | Storage Access Framework |
| `ThumbnailGenerator` | ImageIO / FFmpeg (видео) | MediaMetadataRetriever |

## Навигация (Decompose)

```
RootComponent (ChildStack)
├── LibraryComponent          — главный экран, master-detail
│   ├── grid: состояние сетки, фильтр-вкладки, мультивыбор
│   ├── detail: EditorComponent (ChildSlot) — редактор выбранного файла
│   │   └── LocationPickerComponent (ChildSlot) — диалог с картой
│   └── BatchEditComponent (ChildSlot) — диалог батч-операций
├── MapOverviewComponent      — карта всех фото (фаза 7)
└── SettingsComponent         — настройки (фаза 8)
```

- Верхний уровень — `ChildStack` (Library / Map / Settings).
- Диалоги — `ChildSlot` (закрытие = сброс слота).
- Master-detail: выбранное фото — это состояние `LibraryComponent`, а не навигация. На Android тот же `EditorComponent` монтируется в отдельный экран стека — меняется только Compose-слой, дерево компонентов не трогаем.

## Поток записи метаданных

```
EditorComponent → UpdateCaptureDateUseCase → MetadataRepository:
  1. бэкап оригинального значения (таблица edit_log) 
  2. ExifToolClient.write(file, tags)
  3. контрольное чтение — проверка, что записалось
  4. обновление строки в photo-индексе → Flow эмитит → UI обновился
```

Батч-операции идут через тот же путь по очереди, с прогрессом и отчётом об ошибках по каждому файлу.

## DI (Koin)

- `domainModule` — use case'ы (factory).
- `dataModule` — репозитории, БД, драйвер SQLDelight (single).
- `platformModule` — свой в каждом target: `desktopMain` байндит desktop-реализации интерфейсов.

Компоненты Decompose получают зависимости через конструктор (Koin вызывается один раз на старте в `main()`).

## Структура пакетов (`commonMain`)

```
com.nikolajew.photometadataeditor/
├── domain/
│   ├── model/
│   ├── repository/      # интерфейсы
│   └── usecase/
├── data/
│   ├── db/              # SQLDelight
│   ├── scanner/
│   ├── metadata/        # ExifToolClient (интерфейс), MetadataRepositoryImpl
│   └── repository/
├── ui/
│   ├── root/
│   ├── library/         # grid + detail + batch
│   ├── editor/
│   ├── locationpicker/
│   ├── map/
│   ├── settings/
│   └── theme/
└── di/
```

## Тестирование

- `domain` — чистые unit-тесты в `commonTest` (use case'ы с фейковыми репозиториями).
- `data` — тесты репозиториев с in-memory SQLDelight драйвером.
- Компоненты Decompose — тестируются без UI (это главная причина выбора Decompose).

## Библиотеки

| Библиотека | Назначение |
|---|---|
| Decompose + extensions-compose | навигация, компоненты |
| Koin | DI |
| SQLDelight (+ sqlite-driver) | индекс и статусы |
| kotlinx-coroutines / -datetime | асинхронность, даты |
| Coil 3 (KMP) | загрузка миниатюр в сетке |
| ExifTool (внешний процесс) | чтение/запись метаданных |
