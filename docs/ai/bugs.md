# Известные баги

При ошибке сначала искать здесь. Открытые — подробно, решённые — одной строкой.

## Открытые

- 2026-11-08 — две половины редактора, одну сузить разделителем (`dragby tap split:halves 800 0`): `IllegalArgumentException: maxWidth(-4) must be >= than minWidth(0)` в `LineNumbers.kt:49` (`drawText` с отрицательной шириной у узкой половины). Исключение брошено в отрисовке и убивает цикл отрисовки: окно замирает, оверлей ошибок нарисовать уже нечем ([gotchas.md](gotchas.md)). Найдено пользователем и воспроизведено пультом. Не починено: пользователь сначала проверяет показ ошибок в приложении

## Решённые

<!-- - YYYY-MM-DD — что было — как решили -->
- 2026-11-08 — файл, брошенный из дерева на пустой редактор («Drop files here to open them»), не открывался, если в этой половине с момента запуска не было вкладок: `EditorController.dropOutsideTree` ищет половину по `HalfEditorTabs.rect`, а он заполнялся только у половины с активной вкладкой. Найдено пультом отладки. `rect` теперь задаётся и в ветке пустой половины (`EditorHalf`); сценарий `scripts/scenarios/dnd-empty-editor.txt`
- 2026-11-06 — при вырезании нескольких узлов серым помечался только последний: `cutNoteMarkRecursive` на каждый узел снимал все прошлые пометки ([#27](https://github.com/efimov-r/aisdme/issues/27)). Вырезанные — список файлов в `ProjectTreeState` (`markCut`, `isCut`)
- 2026-11-05 — остаток разбора кода [#26](https://github.com/efimov-r/aisdme/issues/26): потоки, watcher, мелочи, автоскролл [#3](https://github.com/efimov-r/aisdme/issues/3) — PR [#34](https://github.com/efimov-r/aisdme/pull/34); по пунктам — в issue
- 2026-11-05 — конфиги и состояние редактора (#26): снимки уходили в конфиг другого проекта, вкладка с ошибкой отменяла сохранение, закрытые вкладки оставались в кэше — PR #33
- 2026-11-05 — при выходе текст вкладки писался в файл без проверки: незагруженный файл затирался пустой строкой, бинарный портился (#26). `TabCashFileText.disposeBeforeBlocked` пишет только при `WRITABLE` / `NOT_SAVE` и изменённом тексте; в `saveNow` текст берётся один раз
- 2026-11-05 — rename / move / paste молча заменяли существующие файлы (#26). Без `REPLACE_EXISTING`; `paste` проверяет конфликты до начала; диалоги показывают «Already exists» (`FilesExecutor.isTargetTaken`, `findTakenTarget`)
- 2026-11-05 — Windows: переименование только регистром не работало — [gotchas.md](gotchas.md)
- 2026-11-05 — вставка папки в её же подпапку копировала бесконечно, после этого приложение не запускалось: `StackOverflowError` в `ProjectTreeHelper.buildChildren`. Вставка в саму себя запрещена (`FilesExecutor.pasteFromClipboard`), дерево строится не глубже `MAX_DEPTH` = 100, symlink проверяется раньше `isDirectory`
- 2026-11-05 — Windows: папка с подпапками не переименовывалась, `AccessDeniedException` ([#12](https://github.com/efimov-r/aisdme/issues/12)). `WatchService` держит открытой каждую зарегистрированную папку, а Windows не даёт переименовать папку с открытой подпапкой — на Windows регистрируется только корень с `FILE_TREE` (`WindowsPlatform.registerWatch`)
- 2026-11-05 — падение с `NullPointerException` при запуске без последнего проекта ([#2](https://github.com/efimov-r/aisdme/issues/2)) — выбор папки через `JFileChooser` поверх заставки (`AppStartState.ChooseProject`, `ProjectChooserHelper`); ловушка с закрытием приложения — [gotchas.md](gotchas.md)
