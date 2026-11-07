# Архитектура

Как устроено приложение сейчас. Почему так — в [decisions.md](decisions.md). Карта пакетов — в [AGENTS.md](../../AGENTS.md), «Структура кода». Диаграммы C4 — в [docs/architecture.md](../architecture.md).

## Обзор

Слева дерево проекта, справа редактор из двух половин (левой и правой), у каждой свои вкладки. Внизу панель с путём файла и статусом записи. Под редактором — пустая панель на будущее (терминал), справа — узкая панель с переключателем темы. Размеры панелей сохраняются в конфиг проекта.

Состояние хранится в контроллерах. UI получает их через `LocalAppController` (`CompositionLocal`). Контроллеры друг о друге не знают: события между ними идут через `AppEventBus`. Исключения — прямые вызовы: главное окно → `EditorController.checkOpenFiles` (фокус окна), `TreeActions` (дерево, редактор, файлы), `DialogsController` (выделение дерева) и `ProjectTreeState.onDirsLoaded` → `TreeReloadWatcher.dirsLoaded` (шина при переполнении теряла события). Сообщения (ошибки, info, success) контроллеры отдают в `GlobalMessages` через `UiController.subscribeMessages`.

## Запуск

1. `main()` → `AisDmeWindow`: пока читается глобальный конфиг, показывается окно загрузки 500×500 без рамки, по центру экрана. В release окно держится не меньше 2 с, в debug — без задержки.
2. Затем открывается главное окно, его размер и положение берутся из конфига (`rememberPersistentWindowState`). Иконка — `logos/AisDme.png`.
3. `buildAppController` создаёт `AppController`, он — все контроллеры, `reselectProject` (из `LaunchedEffect`, конфиг читается на IO) открывает последний проект: сначала конфиг проекта, затем `TreeReloadWatcher`, дерево и редактор. Пока конфиг не прочитан, главное окно пустое.

## Компоненты

| Компонент | Назначение | Как сделано |
|---|---|---|
| `AppController` | корень композиции | держит все контроллеры, связывает их (сообщения, дерево → watcher), открывает проект (`reselectProject`), `dispose` |
| `CurrentProject` | корень открытого проекта | один `mutableStateOf`: читается и в UI, и из фоновых потоков |
| `ThemeController` | тема | `isDarkTheme`, `toggle()` пишет в глобальный конфиг значение на момент записи |
| `GlobalMessages` | сообщения для `ErrorOverlay` | state-список; `collectFrom` подписывается на `subscribeMessages` контроллеров — сразу, до возврата (`UNDISPATCHED`): у потока `replay = 0`, сообщение без подписчика теряется |
| `TreeActions` | операции над выделенными узлами | открыть в редакторе, показать в файловом менеджере, копировать / вырезать в буфер |
| `EditorController` | открытые файлы и вкладки | две половины `HalfEditorTabs`, у каждой свой список вкладок и активная вкладка. Левая опустела, а в правой есть вкладки — правая переносится в левую: `HalfEditorTabs.onChanged` → `mergeHalvesLater`, после операции целиком |
| `TabCashFileText` | текстовый файл во вкладке | `TextFieldValue` в `mutableStateOf`; автосохранение через 2 с после последнего ввода (корутина с `delay`, прошлая задача отменяется), сохранение перед закрытием вкладки; `Mutex` на запись и сверку с диском; статус записи `WriteStatus`: можно писать, не сохранено, только чтение, заблокирован, ошибка, изменён снаружи, удалён снаружи |
| Сверка файлов с диском | файл, открытый во вкладке, изменили или удалили снаружи | `TabCashFile.diskStamp` — время правки и размер, взятые при чтении и после своей записи. `EditorController.checkOpenFiles` (по очереди, `checkMutex`) зовёт `checkDisk` у вкладок: при фокусе окна (`Main.kt`) и по `RebuilDirRiff` (вкладки внутри папки). Текст без правок перечитывается; есть несохранённый ввод — `WriteStatus.CONFLICT`; удалён — вкладка без ввода закрывается, с вводом `DELETED`. Выбор — плашка `ExternalChangeBanner`, `TabCashFileText.resolve(ExternalChoice)`. Перед каждой записью (`saveNow`) и при выходе тот же отпечаток проверяется: внешняя правка не затирается |
| `TabCashFileImg` | изображение во вкладке | зум к курсору, Ctrl + «+» / «−», перетаскивание средней кнопкой мыши; показываются размеры, есть кроп. Файл изменили снаружи — картинка перечитывается, удалили — вкладка закрывается |
| `TabCashPaste` | вкладка из содержимого буфера обмена | сохраняется в отдельную папку кэша вкладок |
| `ProjectController`, `ProjectTreeState` | дерево файлов | узлы `ProjectNode` — файл и глубина, у `DirNode` ещё `children`, `expanded`, `loaded`; `LazyColumn` из видимых узлов. Состояние вида — в `ProjectTreeState`, по файлам, а не в узлах: выделение (`TreeSelection`), вырезанные (`cutFiles`, узел помечен, если внутри одного из них), позиции для меню по клавише; файлы, которых нет в дереве, забываются после чтения папок. Ленивая загрузка: свёрнутая папка с диска не читается (`DirNode.loaded = false`), содержимое читается при первом раскрытии (подписка `snapshotFlow` в `ProjectController`), «показать в дереве» читает папки на пути, «раскрыть всё» — всё поддерево. Диск — на IO, слияние (`applyDirDiff`) — на UI-потоке, по очереди (`diskMutex`); при событии перечитываются папка и прочитанные вложенные |
| `TreeReloadWatcher` | изменения на диске | `WatchService`, только `CREATE` / `DELETE`. Windows — только корень с `FILE_TREE` (`IPlatform.watchSubtree`); Linux — корень и прочитанные в дерево папки (`ProjectTreeState.onDirsLoaded` → `dirsLoaded`, связаны в `AppController` напрямую, не шиной). Смена проекта и регистрации — одна очередь команд. Отложенная (200 мс) перезагрузка изменённых папок одним обработчиком пакетов, сигнал — conflated `Channel`; папки нет в дереве — перечитывается ближайший предок |
| `FilesController`, `FilesExecutor` | файловые операции | создать, удалить, переименовать, скопировать, переместить; проверка имени (`FileNameValidator`). Принимают `Path`, от узлов дерева не зависят; папка или файл — по диску (`NOFOLLOW_LINKS`) |
| Буфер обмена | копировать, вырезать, вставить | `FileTransferable`: файлы — `javaFileListFlavor` и `text/uri-list`; пометка «вырезано» — формат ОС из `IPlatform.clipboardCutNative` (Windows — `Preferred DropEffect`, KDE — `application/x-kde-cutselection`), связан с `cutFlavor` через `SystemFlavorMap`. Работает с Explorer в обе стороны |
| `MoveController` | drag & drop | перетаскивание внутри дерева и из дерева в редактор с подсветкой цели; `DragPreviewLayer` — превью; автоскролл у края |
| `ConfigStore<T>` | конфиги | kotlinx.serialization, атомарная запись, `Mutex`, кэш; пишет на диск при каждом `update`, без задержки |
| `DialogsController` | диалоги и подсказки | новый файл, переименование, удаление, перемещение, вставка; `DialogsStarter`, `TooltipStarter`. Выделение проверяется при запросе (`requestPasteDialog` и др.): не подходит — ошибка, диалог не открывается; узлы передаются в `UiDialog` |
| `Platform` | всё, что зависит от ОС | интерфейс `IPlatform`, реализации `WindowsPlatform` и `LinuxPlatform` (macOS тоже идёт в неё); `Platform.current` выбирается по `os.name` один раз. Остальной код от ОС не зависит |
| `AisDmeTheme` | тема | светлая и тёмная, цвета через `LocalAisDmeColors`, фирменный зелёный `0xFF35F074`; вкладки в форме трапеции |

## Конфиги

| Конфиг | Где лежит | Что в нём |
|---|---|---|
| Глобальный (`AppConfig`) | Linux — `~/.config/AisDme/config.json`, Windows — `%APPDATA%\AisDme\config.json` | тема, последний открытый проект, окно |
| Проекта (`ProjectConfig`) | `<корень проекта>/.aisdme/config.json` | состояние дерева (только корень и раскрытые папки) и редактора (открытые вкладки — путь и тип), размеры панелей (`keyValue`) |

Пути — в `ConfigPaths`, папка конфигов ОС — `IPlatform.configRoot()`.

Сохранение состояния проекта: `EditorController` (снимки вкладок `SnapshotTabCash`, после открытия файла) и `ProjectController` пишут в `EditorStateRepository` и `TreeStateRepository` присваиванием `value`, `ConfigController` собирает из них снимки (`SnapshotEditor`, `SnapshotTree`) и пишет с задержкой (`debounce`: редактор — 4 с, дерево — 2 с). Задачи автосохранения запускаются на каждый проект со своим `ConfigStore`; при смене проекта и выходе они останавливаются, текущее состояние сразу пишется в конфиг этого проекта, репозитории сбрасываются. В конфиг попадают только открытые вкладки: закрытая удаляется из кэша `EditorController` (`removeClosedTabs`), перед этим `beforeRemove` сохраняет текст или удаляет `.clip`.

## Редактор

- Основа — `BasicTextField`: он только принимает ввод. Номера строк рисуются отдельным `Canvas` по `TextLayoutResult` синхронно со скроллом. Есть горизонтальный скролл и прокрутка к курсору.
- Если файл нельзя изменять, включается режим «только чтение».
- Весь текст файла хранится в одном `TextFieldValue`: для больших файлов это медленно. План замены — [todo.md](todo.md), решение — [decisions.md](decisions.md).

## Сборка

- Gradle Kotlin DSL, один модуль.
- Release или debug — Gradle property `-Prelease`. Задача `generateBuildConfig` пишет `BuildConfig.DEBUG` в `build/generated/buildconfig`, эта папка подключена как source set.

