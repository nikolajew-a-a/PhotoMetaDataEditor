# Диаграммы архитектуры

Три уровня детализации: модули → UI-компоненты → классы и связи.
Актуально на фазу 4 (просмотр, статусы, редактирование метаданных, карта, удаление).

## 1. Модули

Обе стрелки зависимостей смотрят на `:domain` — он не знает никого.
Пунктирная связь `:composeApp → :data` существует только ради DI: composition root
должен видеть реализации, чтобы связать их с интерфейсами через Koin.

```mermaid
flowchart TB
    subgraph composeApp [":composeApp — UI и composition root"]
        ui["ui: root · library · map · locationpicker"]
        di["di: модули Koin"]
        platform["platform: FolderPicker (порт)"]
        desktopUi["desktopMain: Main.kt · DesktopFolderPicker · DesktopModule"]
    end

    subgraph domain [":domain — чистый Kotlin, ноль внешних зависимостей"]
        models["Модели: Photo · GeoPoint · MediaType · LibraryFilter"]
        usecases["Use case'ы: по классу на операцию (6 шт.)"]
        repoIfaces["Интерфейсы: PhotoLibraryRepository · MetadataRepository"]
    end

    subgraph data [":data — реализации и хранение"]
        repos["PhotoLibraryRepositoryImpl · MetadataRepositoryImpl"]
        db["SQLDelight: photoEntity · editLog"]
        ports["Порты: MediaFileScanner · MetadataEngine · FileDeleter · FileExistenceChecker"]
        desktopData["desktopMain: процесс ExifTool · Files.walk · корзина · JDBC-драйвер"]
    end

    subgraph external ["внешний мир"]
        exiftool["exiftool.exe (stay_open)"]
        sqlite["photoindex.db (%APPDATA%)"]
        osm["OSM-тайлы (через Coil)"]
        folder["папка с фотографиями"]
    end

    composeApp --> domain
    data --> domain
    composeApp -. "только DI" .-> data
    data --> external
```

## 2. Дерево UI-компонентов (Decompose)

Логика экранов живёт в компонентах — обычных Kotlin-классах, тестируемых без UI.
Compose-функции только рисуют `State` и шлют события обратно.
`LocationPicker` создаётся по требованию (слот пуст, пока не нажали «Выбрать на карте»)
и умирает при закрытии диалога.

```mermaid
flowchart TB
    main["Main.kt (desktopMain)<br/>startKoin · LifecycleRegistry · окно"]
    root["DefaultRootComponent<br/>ChildStack: Library | Map"]
    lib["LibraryComponent<br/>сетка · фильтры · правки · удаление"]
    map["MapOverviewComponent<br/>все фото с геолокацией"]
    picker["LocationPickerComponent<br/>диалог выбора точки"]
    tilemap["TileMapView<br/>общий виджет карты (OSM-тайлы)"]
    content["Compose-слой: RootContent · LibraryContent · MapOverviewContent · LocationPickerDialog"]

    main --> root
    root --> lib
    root --> map
    lib -. "ChildSlot" .-> picker
    map --> tilemap
    picker --> tilemap
    lib ~~~ content
```

## 3. Классы и интерфейсы domain/data

Сплошная стрелка — «вызывает», пунктирная — «реализует» (конкретный класс подставляет Koin).

```mermaid
flowchart TB
    ucLib["OpenFolder · ObserveLibrary · SetProcessed · DeletePhoto<br/>(use case'ы, :domain)"]
    ucMeta["UpdateCaptureDate · UpdateLocation<br/>(use case'ы, :domain)"]
    plr["PhotoLibraryRepository<br/>(интерфейс, :domain)"]
    mr["MetadataRepository<br/>(интерфейс, :domain)"]
    plrImpl["PhotoLibraryRepositoryImpl<br/>скан · сверка с диском · статусы"]
    mrImpl["MetadataRepositoryImpl<br/>журнал → запись → контрольное чтение"]

    scanner["MediaFileScanner<br/>(порт)"]
    engine["MetadataEngine<br/>(порт)"]
    files["FileDeleter · FileExistenceChecker<br/>(порты)"]
    photoDb["PhotoIndexLocalDataSource"]
    editDb["EditLogLocalDataSource"]
    database["PhotoIndexDatabase<br/>(генерирует SQLDelight)"]

    dScanner["DesktopMediaFileScanner<br/>(Files.walk)"]
    dEngine["ExifToolMetadataEngine → ExifToolProcess<br/>→ exiftool.exe"]
    dFiles["DesktopFileDeleter (корзина)<br/>DesktopFileExistenceChecker"]
    dDriver["DesktopDatabaseDriverFactory<br/>(JDBC + миграции)"]

    ucLib --> plr
    ucMeta --> mr
    plrImpl -.-> plr
    mrImpl -.-> mr
    plrImpl --> scanner
    plrImpl --> photoDb
    plrImpl --> files
    plrImpl --> engine
    mrImpl --> engine
    mrImpl --> photoDb
    mrImpl --> editDb
    photoDb --> database
    editDb --> database
    dScanner -.-> scanner
    dEngine -.-> engine
    dFiles -.-> files
    database --> dDriver
```

### Пример пути одного действия — «пользователь сохранил дату»

1. `LibraryComponent.onSaveCaptureDate` парсит ввод →
2. `UpdateCaptureDateUseCase` →
3. интерфейс `MetadataRepository` → (Koin подставил) `MetadataRepositoryImpl`:
   старое значение в `editLog` → `MetadataEngine.writeMetadata` →
   (Koin подставил) `ExifToolMetadataEngine` → `ExifToolProcess` → `exiftool.exe` →
   контрольное чтение → `PhotoIndexLocalDataSource.updateTakenAt` →
4. SQLite эмитит новый `Flow` → сетка и панель перерисовываются сами.

### Особые случаи

- `FolderPicker` — единственный порт вне `:data` (живёт в `:composeApp/platform`),
  потому что выбор папки — UI-взаимодействие, а не работа с данными.
- Библиотечный `Flow` самовосстанавливается: каждая выдача сверяется с диском,
  пропавшие файлы скрываются и удаляются из индекса.
