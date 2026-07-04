# Дизайн: клик по маркеру карты

## Context

`TileMapView` (ui/map/TileMapView.kt) рисует маркеры как `Box` со смещением,
рассчитанным из координат; клики сейчас перехватывает только фон карты
(`detectTapGestures` → `onMapClick`). `MapOverviewComponent` — компонент без
выходов, `RootComponent` держит `ChildStack` (Library | Map) и создаёт
компоненты, разрешая зависимости из Koin. Выбор фото — состояние
`LibraryComponent` (`selectedPhotoId`).

## Goals / Non-Goals

**Goals:**
- Клик по маркеру → экран «Библиотека» с выбранным фото.
- Не сломать выбор точки в LocationPicker (он использует тот же TileMapView).

**Non-Goals:**
- Кластеризация, тултипы, автопрокрутка сетки к плитке (TODO на будущее).

## Decisions

1. **Клик на маркере, а не хит-тест в onMapClick.** Каждый маркер-`Box` получает
   собственный `Modifier.clickable`, когда передан `onMarkerClick`. Compose сам
   разрулит приоритет над фоновым `detectTapGestures` (дочерний элемент выше
   в z-порядке). Альтернатива — вычислять попадание в `onMapClick` по расстоянию
   до ближайшего маркера — отвергнута: дублирует то, что Compose даёт бесплатно,
   и требует ручной настройки радиуса попадания.
2. **Сигнатура**: `TileMapView(..., onMarkerClick: ((MapMarker) -> Unit)? = null)`.
   `null` (по умолчанию и в LocationPicker) — маркеры не кликабельны, поведение
   не меняется.
3. **Навигация через выход компонента (Decompose-канон)**: у
   `DefaultMapOverviewComponent` появляется конструктор-параметр
   `onPhotoSelected: (photoId: String) -> Unit`. `RootComponent` передаёт лямбду:
   `navigation.bringToFront(Config.Library)` + вызов `selectPhoto(id)` у
   Library-компонента. Альтернатива — общий «шина событий»/shared state —
   отвергнута как лишняя сущность.
4. **Доступ к LibraryComponent из RootComponent.** При `bringToFront` компонент
   Library уже существует в стеке (или создаётся). RootComponent находит его
   в `stack.value` после навигации и вызывает `selectPhoto(id)`. Если в момент
   клика Library ещё не создан — RootComponent запоминает `pendingPhotoId`
   и передаёт его в фабрику при создании (поле в конструкторе
   `DefaultLibraryComponent(initialSelectedId: String? = null)`).

## Risks / Trade-offs

- [Маркеры перекрываются при одном зуме] → кликается верхний (последний
  в списке); это допустимо до кластеризации.
- [selectPhoto на фото, скрытое активным фильтром] → выбор работает по полному
  списку (`photos`, не `visiblePhotos`), панель покажет фото; при желании
  пользователь сам переключит вкладку. Фильтр не сбрасываем автоматически.

## Open Questions

- Прокрутка сетки к выбранной плитке — отложено (см. non-goals).
