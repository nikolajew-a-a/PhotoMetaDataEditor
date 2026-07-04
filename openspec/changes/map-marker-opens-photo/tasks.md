# Задачи: клик по маркеру карты открывает фото

## 1. TileMapView — кликабельные маркеры

- [ ] 1.1 Добавить в `TileMapView` параметр `onMarkerClick: ((MapMarker) -> Unit)? = null`;
      при ненулевом колбэке маркер-`Box` получает `Modifier.clickable`. Убедиться,
      что `LocationPickerDialog` (без колбэка) ведёт себя как раньше. `gradlew build`.

## 2. Компоненты — выход из Map в Library

- [ ] 2.1 `DefaultMapOverviewComponent`: параметр `onPhotoSelected: (String) -> Unit`,
      прокинуть в `MapOverviewContent` → `TileMapView(onMarkerClick = { onPhotoSelected(it.id) })`.
- [ ] 2.2 `LibraryComponent`: публичный метод `selectPhoto(id: String)` (обновляет
      `selectedPhotoId`); `DefaultLibraryComponent` — параметр
      `initialSelectedId: String? = null` для случая, когда Library создаётся заново.
- [ ] 2.3 `DefaultRootComponent`: лямбда для Map — `bringToFront(Config.Library)`,
      затем найти Library в стеке и вызвать `selectPhoto(id)`; если компонент
      создаётся заново — передать `initialSelectedId`. `gradlew build`.

## 3. Проверка по спеке и завершение

- [ ] 3.1 Ручная проверка трёх сценариев спеки map-overview: клик по маркеру,
      маркер при активном фильтре «Обработанные», клик в LocationPicker.
- [ ] 3.2 Коммит + push; `openspec archive map-marker-opens-photo` (спека уезжает
      в openspec/specs/map-overview/), закоммитить архивацию.
