# Архитектура AisDme в диаграммах

Описание архитектуры по [C4 Model](https://c4model.com/): от системы целиком до компонентов внутри приложения. Диаграммы нарисованы в Mermaid, GitHub показывает их прямо в тексте. Они описывают код на 2026-11-07, а не целевую архитектуру. Уровень 4 (Code) не рисуется: классы смотрятся в IDE.

Текстовое описание компонентов — в [docs/ai/architecture.md](ai/architecture.md), причины решений — в [docs/ai/decisions.md](ai/decisions.md).

Обозначения на всех диаграммах:

| Элемент | Что это |
|---|---|
| Person | человек, пользователь IDE |
| System | AisDme целиком |
| System_Ext | внешняя система, не часть приложения |
| Container, ContainerDb | отдельно запускаемое приложение или хранилище данных |
| Component | класс или группа классов внутри приложения |
| Стрелка с подписью | кто кого вызывает или читает; под подписью — технология |

## 1. System Context

Кто пользуется AisDme и с чем она связана вне себя.

```mermaid
C4Context
    title Системный контекст AisDme

    Person(user, "Пользователь", "Открывает проект, правит файлы, смотрит изображения")
    System(aisdme, "AisDme", "Учебная IDE на Kotlin и Compose for Desktop")
    System_Ext(fs, "Файловая система", "Файлы проекта и конфиги IDE")
    System_Ext(fm, "Файловый менеджер ОС", "Explorer, Dolphin, Nautilus, Thunar, PCManFM")
    System_Ext(clipboard, "Буфер обмена ОС", "Файлы и текст; пометка «вырезано» в формате своей ОС")

    Rel(user, aisdme, "Работает в IDE")
    Rel(aisdme, fs, "Читает, пишет, переносит файлы; следит за изменениями", "java.nio.file, WatchService")
    Rel(aisdme, fm, "Показывает файл в менеджере", "ProcessBuilder")
    BiRel(aisdme, clipboard, "Копирует и вставляет файлы и текст", "AWT Clipboard")
    Rel(fm, clipboard, "Кладёт и берёт файлы", "формат ОС")
    Rel(fm, fs, "Меняет файлы извне, AisDme это замечает")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## 2. Container

Приложение — один процесс JVM. Всё остальное — данные на диске: два конфига и кэш вкладок «из буфера обмена».

```mermaid
C4Container
    title Контейнеры AisDme

    Person(user, "Пользователь", "Работает в IDE")

    System_Boundary(aisdme, "AisDme") {
        Container(app, "Desktop-приложение", "Kotlin, Compose for Desktop, JVM 17", "Окно, дерево проекта, редактор, диалоги; контроллеры и шина событий")
        ContainerDb(globalCfg, "Глобальный конфиг", "JSON, config.json", "Тема, последний проект, окно. Папка конфигов ОС: Linux ~/.config/AisDme, Windows %APPDATA%/AisDme")
        ContainerDb(projectCfg, "Конфиг проекта", "JSON, .aisdme/config.json", "Дерево (корень и раскрытые папки), открытые вкладки, размеры панелей")
        ContainerDb(clipCache, "Кэш вкладок из буфера", "Файлы .clip, .aisdme/pastedtab", "Текст вкладок «From Clipboard» между запусками")
    }

    System_Ext(projectFiles, "Файлы проекта", "Файловая система")
    System_Ext(fm, "Файловый менеджер ОС", "Explorer, Dolphin и др.")
    System_Ext(clipboard, "Буфер обмена ОС", "Файлы и текст")

    Rel(user, app, "Работает в окне")
    Rel(app, projectFiles, "Читает, пишет, переносит; следит за изменениями", "java.nio.file, WatchService")
    Rel(app, globalCfg, "Читает при старте, пишет при смене темы и проекта", "kotlinx.serialization, атомарная запись")
    Rel(app, projectCfg, "Читает при открытии проекта, пишет с задержкой (debounce)", "kotlinx.serialization, атомарная запись")
    Rel(app, clipCache, "Пишет при выходе, читает при открытии, удаляет при закрытии вкладки", "java.nio.file")
    Rel(app, fm, "Показать файл в менеджере", "ProcessBuilder")
    BiRel(app, clipboard, "Копировать, вырезать, вставить", "AWT Clipboard")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## 3. Component

Приложение разбито на две диаграммы: на одной контроллеры и события между ними, на другой данные, файлы и всё, что зависит от ОС. Одна диаграмма со всеми компонентами получилась бы перегруженной.

Чего на диаграммах нет:

- Compose UI показан одним элементом: экраны получают контроллеры через `LocalAppController`.
- `AppController` создаёт все контроллеры и передаёт им шину. Стрелки к каждому не рисуются.
- `UiController` — абстрактный предок контроллеров (корутины и поток сообщений), отдельным компонентом не показан. `GlobalMessages` подписывается на сообщения всех контроллеров (`collectFrom`) и показывает их в `ErrorOverlay`.
- `MoveController` (состояние drag & drop) связан только с UI и нигде не показан.

### 3.1 Контроллеры и события

```mermaid
C4Component
    title Компоненты: контроллеры и события

    Container_Boundary(app, "Desktop-приложение") {
        Component(ui, "UI", "Compose for Desktop", "Дерево, редактор, диалоги, меню")
        Component(appC, "AppController", "Kotlin", "Корень композиции: создаёт контроллеры и шину, открывает проект")
        Component(bus, "AppEventBus", "MutableSharedFlow", "События: FileOpened, ShowFileInTree, RebuilDirRiff, MoveFile, DeleteFile, DeleteDir")
        Component(projectC, "ProjectController", "Kotlin, ProjectTreeState", "Дерево файлов: узлы, выделение, ленивая загрузка папок")
        Component(editorC, "EditorController", "Kotlin", "Две половины редактора, вкладки TabCash*")
        Component(filesC, "FilesController", "Kotlin", "Создать, удалить, переименовать, переместить, вставить")
        Component(watcher, "TreeReloadWatcher", "WatchService", "Изменения на диске пачкой раз в 200 мс")
        Component(treeActions, "TreeActions", "Kotlin", "Операции над выделенными узлами дерева")
        Component(dialogsC, "DialogsController", "Kotlin", "Диалоги: новый файл, переименование, удаление, вставка")
    }

    Rel(ui, appC, "Берёт контроллеры", "LocalAppController")
    Rel(appC, bus, "Создаёт шину и передаёт контроллерам")

    Rel(projectC, bus, "Двойной клик по файлу", "FileOpened")
    Rel(bus, editorC, "Открыть вкладку; файл переехал или удалён", "FileOpened, MoveFile, DeleteFile, DeleteDir")
    Rel(editorC, bus, "Показать файл вкладки в дереве", "ShowFileInTree")
    Rel(filesC, bus, "Файл переехал или удалён", "MoveFile, DeleteFile, DeleteDir")
    Rel(watcher, bus, "Папка изменилась на диске", "RebuilDirRiff")
    Rel(bus, projectC, "Перечитать папку; показать файл", "RebuilDirRiff, ShowFileInTree")
    Rel(projectC, watcher, "Прочитанные папки: на Linux следить за ними", "dirsLoaded, напрямую")

    Rel(treeActions, projectC, "Выделенные узлы; пометка «вырезано»", "getSelectedNodes, markCut")
    Rel(treeActions, editorC, "Открыть выделенные в половине", "openNodes")
    Rel(treeActions, filesC, "Копировать в буфер", "copyToClipboard")
    Rel(dialogsC, projectC, "Выделенные узлы для диалога", "getSelectedNodes")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

### 3.2 Данные, файлы и платформа

```mermaid
C4Component
    title Компоненты: данные, файлы и платформа

    Container_Boundary(app, "Desktop-приложение") {
        Component(editorC, "EditorController", "Kotlin", "Вкладки редактора")
        Component(projectC, "ProjectController", "Kotlin", "Дерево файлов")
        Component(themeC, "ThemeController", "Kotlin", "Светлая и тёмная тема")
        Component(configC, "ConfigController", "Kotlin", "Автосохранение снимков на каждый проект")
        Component(editorRep, "EditorStateRepository", "MutableStateFlow", "Снимок вкладок")
        Component(treeRep, "TreeStateRepository", "MutableStateFlow", "Снимок дерева")
        Component(store, "ConfigStore", "kotlinx.serialization", "Чтение и атомарная запись JSON, Mutex, кэш")
        Component(filesC, "FilesController", "Kotlin", "Файловые операции и буфер обмена")
        Component(executor, "FilesExecutor", "java.nio.file", "Создать, копировать, перенести, удалить; буфер обмена")
        Component(transferable, "FileTransferable", "AWT datatransfer", "Файлы и пометка «вырезано» в буфере")
        Component(watcher, "TreeReloadWatcher", "WatchService", "События CREATE и DELETE")
        Component(platform, "Platform", "IPlatform: WindowsPlatform, LinuxPlatform", "Всё, что зависит от ОС")
    }

    ContainerDb(globalCfg, "Глобальный конфиг", "JSON", "Папка конфигов ОС")
    ContainerDb(projectCfg, "Конфиг проекта", "JSON", ".aisdme/config.json")
    System_Ext(fs, "Файловая система", "Файлы проекта")
    System_Ext(clipboard, "Буфер обмена ОС", "Файлы и текст")
    System_Ext(fm, "Файловый менеджер ОС", "Explorer, Dolphin и др.")

    Rel(editorC, editorRep, "Пишет снимок вкладок", "value =")
    Rel(projectC, treeRep, "Пишет снимок дерева", "value =")
    Rel(configC, editorRep, "Читает снимок", "debounce 4 с")
    Rel(configC, treeRep, "Читает снимок", "debounce 2 с")
    Rel(configC, store, "Обновляет конфиг проекта и последний проект", "update")
    Rel(themeC, store, "Пишет тему в глобальный конфиг", "update")
    Rel(store, globalCfg, "Читает и пишет", "JSON")
    Rel(store, projectCfg, "Читает и пишет", "JSON")
    Rel(store, platform, "Папка конфигов ОС", "configRoot, ConfigPaths")

    Rel(filesC, executor, "Операции над Path")
    Rel(executor, fs, "Читает, копирует, переносит, удаляет", "java.nio.file")
    Rel(executor, transferable, "Создаёт содержимое для буфера")
    BiRel(executor, clipboard, "Кладёт и берёт файлы", "AWT Clipboard")
    Rel(transferable, platform, "Формат и байты пометки «вырезано»", "clipboardCutNative")
    Rel(executor, platform, "Распознаёт «вырезано»", "isClipboardCut")

    Rel(watcher, platform, "Регистрирует папки", "registerWatch")
    Rel(platform, fs, "Следит за папками", "WatchService, FILE_TREE на Windows")
    Rel(platform, fm, "Показывает файл", "showInFileManager")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

## 4. Dynamic

### 4.1 Открытие файла из дерева

Двойной клик по файлу открывает вкладку, а состояние редактора попадает в конфиг проекта.

```mermaid
C4Dynamic
    title Открытие файла двойным кликом

    Person(user, "Пользователь", "Работает в IDE")
    Component(projectC, "ProjectController", "Kotlin", "Дерево файлов")
    Component(bus, "AppEventBus", "MutableSharedFlow", "События между контроллерами")
    Component(editorC, "EditorController", "Kotlin", "Вкладки редактора")
    Component(tab, "TabCashFileText", "Kotlin", "Текстовый файл во вкладке")
    System_Ext(fs, "Файловая система", "Файлы проекта")
    Component(editorRep, "EditorStateRepository", "MutableStateFlow", "Снимок вкладок")
    Component(configC, "ConfigController", "Kotlin", "Автосохранение снимков")
    ContainerDb(projectCfg, "Конфиг проекта", "JSON", ".aisdme/config.json")

    RelIndex(1, user, projectC, "Двойной клик по файлу")
    RelIndex(2, projectC, bus, "FileOpened")
    RelIndex(3, bus, editorC, "FileOpened, на UI-потоке")
    RelIndex(4, editorC, tab, "Создаёт вкладку и открывает файл")
    RelIndex(5, tab, fs, "Читает текст", "java.io.File")
    RelIndex(6, editorC, editorRep, "Пишет снимок вкладок")
    RelIndex(7, configC, editorRep, "Читает снимок, debounce 4 с")
    RelIndex(8, configC, projectCfg, "Пишет конфиг проекта", "ConfigStore")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

### 4.2 Изменение на диске извне

Файл создали в файловом менеджере: дерево обновляется без действий пользователя. На Windows watcher следит за всем деревом одним ключом, на Linux — только за корнем и папками, прочитанными в дерево.

```mermaid
C4Dynamic
    title Обновление дерева после изменения на диске

    System_Ext(fs, "Файловая система", "Файлы проекта")
    Component(watcher, "TreeReloadWatcher", "WatchService", "Изменения на диске")
    Component(bus, "AppEventBus", "MutableSharedFlow", "События между контроллерами")
    Component(projectC, "ProjectController", "Kotlin", "Дерево файлов")
    Component(treeState, "ProjectTreeState", "Kotlin", "Узлы, выделение, слияние с диском")
    Component(ui, "UI", "Compose for Desktop", "Дерево на экране")

    RelIndex(1, fs, watcher, "ENTRY_CREATE, ENTRY_DELETE", "WatchService")
    RelIndex(2, watcher, bus, "Через 200 мс одним пакетом: RebuilDirRiff для каждой папки, которая ещё есть")
    RelIndex(3, bus, projectC, "RebuilDirRiff")
    RelIndex(4, projectC, treeState, "rebuildDirDiff")
    RelIndex(5, treeState, fs, "Читает прочитанные папки заново", "listFiles")
    RelIndex(6, treeState, ui, "Новые узлы, state обновляется на UI-потоке")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Расхождения с решениями

- **«Контроллеры общаются через шину».** В [docs/ai/architecture.md](ai/architecture.md) это сказано без оговорок. По коду шина — основной, но не единственный путь:
  - прочитанные папки дерева идут в watcher напрямую (`ProjectTreeState.onDirsLoaded` → `TreeReloadWatcher.dirsLoaded`, связка в `AppController`). Это решение записано в [decisions.md](ai/decisions.md), «Ленивая загрузка дерева проекта»: шина при переполнении теряла события;
  - `TreeActions` и `DialogsController` вызывают контроллеры напрямую.
- **`UiController`** в issue [#35](https://github.com/efimov-r/aisdme/issues/35) назван контроллером. По коду это абстрактный предок: корутины и поток сообщений для всех контроллеров. Отдельным компонентом он не нарисован.
- **Сообщения об ошибках** на схемах не показаны: каждый контроллер отдаёт их в `GlobalMessages`, а он показывает в `ErrorOverlay`. Это сквозная связь, она перегрузила бы диаграммы.
