# Известные баги

При ошибке сначала искать здесь. Открытые — подробно, решённые — одной строкой.

## Открытые


### Баги из разбора кода (2026-11-05)
- **Симптом:** список из ~20 пунктов: конфиги и снимки, потоки, watcher, мелочи. Потеря данных исправлена (см. «Решённые»)
- **Где:** список с путями и строками — в issue [#26](https://github.com/efimov-r/aisdme/issues/26)
- **Статус:** открыт; найдено по коду, запуском не проверено
- **Что пробовали:** ничего

## Решённые

<!-- - YYYY-MM-DD — что было — как решили -->
- 2026-11-05 — при выходе текст вкладки писался в файл без проверки: незагруженный файл затирался пустой строкой, бинарный портился (#26). `TabCashFileText.disposeBeforeBlocked` пишет только при `WRITABLE` / `NOT_SAVE` и изменённом тексте; в `saveNow` текст берётся один раз
- 2026-11-05 — rename / move / paste молча заменяли существующие файлы (#26). Без `REPLACE_EXISTING`; `paste` проверяет конфликты до начала; диалоги показывают «Already exists» (`FilesExecutor.isTargetTaken`, `findTakenTarget`)
- 2026-11-05 — Windows: переименование только регистром не работало — [gotchas.md](gotchas.md)
- 2026-11-05 — вставка папки в её же подпапку копировала бесконечно, после этого приложение не запускалось: `StackOverflowError` в `ProjectTreeHelper.buildChildren`. Вставка в саму себя запрещена (`FilesExecutor.pasteFromClipboard`), дерево строится не глубже `MAX_DEPTH` = 100, symlink проверяется раньше `isDirectory`
- 2026-11-05 — Windows: папка с подпапками не переименовывалась, `AccessDeniedException` ([#12](https://github.com/efimov-r/aisdme/issues/12)). `WatchService` держит открытой каждую зарегистрированную папку, а Windows не даёт переименовать папку с открытой подпапкой — на Windows регистрируется только корень с `FILE_TREE` (`WindowsPlatform.registerWatch`)
- 2026-11-05 — падение с `NullPointerException` при запуске без последнего проекта ([#2](https://github.com/efimov-r/aisdme/issues/2)) — выбор папки через `JFileChooser` поверх заставки (`AppStartState.ChooseProject`, `ProjectChooserHelper`); ловушка с закрытием приложения — [gotchas.md](gotchas.md)
