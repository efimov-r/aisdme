# Архитектура AisDme в диаграммах

Описание архитектуры по [C4 Model](https://c4model.com/): от системы целиком до компонентов внутри приложения. Диаграммы нарисованы в Mermaid (`flowchart` и `sequenceDiagram`): GitHub показывает их прямо в тексте. Родной синтаксис C4 в Mermaid не раскладывает элементы сам и клал подписи связей друг на друга, поэтому уровни и типы элементов C4 сохранены, а рисуются они обычными диаграммами. Диаграммы описывают код на 2026-11-07, а не целевую архитектуру. Уровень 4 (Code) не рисуется: классы смотрятся в IDE.

Текстовое описание компонентов — в [docs/ai/architecture.md](ai/architecture.md), причины решений — в [docs/ai/decisions.md](ai/decisions.md).

Обозначения на схемах:

| Вид | Что это |
|---|---|
| тёмно-синий овал | человек, пользователь IDE |
| синий прямоугольник | система AisDme или её контейнер |
| серый прямоугольник | внешняя система, не часть AisDme |
| голубой прямоугольник | компонент внутри приложения |
| зелёный цилиндр | хранилище данных на диске, часть AisDme |
| оранжевый прямоугольник | шина событий `AppEventBus` |
| сплошная стрелка | вызов, чтение, запись или событие через шину |
| пунктирная стрелка | прямой вызов между компонентами, не через шину |
| стрелка с двумя остриями | данные идут в обе стороны |

## 1. System Context

Кто пользуется AisDme и с чем она связана вне себя.

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 45, "rankSpacing": 110}}}%%
flowchart LR
    classDef person fill:#08427b,stroke:#052e56,color:#fff
    classDef system fill:#1168bd,stroke:#0b4884,color:#fff
    classDef ext fill:#6b6b6b,stroke:#4a4a4a,color:#fff

    user(["<b>Пользователь</b><br/>Person"]):::person
    aisdme["<b>AisDme</b><br/>учебная IDE<br/>Kotlin, Compose for Desktop"]:::system
    fs["<b>Файловая система</b><br/>файлы проекта и конфиги"]:::ext
    fm["<b>Файловый менеджер ОС</b><br/>Explorer, Dolphin и др."]:::ext
    clipboard["<b>Буфер обмена ОС</b><br/>файлы и текст"]:::ext

    user -->|"работает"| aisdme
    aisdme <-->|"читает, пишет, следит"| fs
    aisdme -->|"показать файл"| fm
    aisdme <-->|"копировать, вставить"| clipboard
```

| Связь | Технология |
|---|---|
| AisDme ↔ файловая система | `java.nio.file`, `WatchService` |
| AisDme → файловый менеджер | `ProcessBuilder` |
| AisDme ↔ буфер обмена | AWT `Clipboard`, пометка «вырезано» в формате ОС |

Файловый менеджер и AisDme общаются не напрямую: менеджер меняет файлы на диске (AisDme замечает это через `WatchService`) и кладёт файлы в буфер обмена, откуда AisDme их вставляет, в том числе с пометкой «вырезано».

## 2. Container

Приложение — один процесс JVM. Всё остальное — данные на диске: два конфига и кэш вкладок «из буфера обмена». Зелёные хранилища принадлежат AisDme, серые системы — внешние.

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 40, "rankSpacing": 140}}}%%
flowchart LR
    classDef person fill:#08427b,stroke:#052e56,color:#fff
    classDef system fill:#1168bd,stroke:#0b4884,color:#fff
    classDef ext fill:#6b6b6b,stroke:#4a4a4a,color:#fff
    classDef db fill:#2b7a4b,stroke:#1d5434,color:#fff

    user(["<b>Пользователь</b><br/>Person"]):::person
    app["<b>Desktop-приложение</b><br/>Kotlin, Compose for Desktop<br/>JVM 17"]:::system
    globalCfg[("<b>Глобальный конфиг</b><br/>config.json")]:::db
    projectCfg[("<b>Конфиг проекта</b><br/>.aisdme/config.json")]:::db
    clipCache[("<b>Кэш вкладок из буфера</b><br/>.aisdme/pastedtab/*.clip")]:::db
    projectFiles["<b>Файлы проекта</b><br/>файловая система"]:::ext
    fm["<b>Файловый менеджер ОС</b><br/>Explorer, Dolphin и др."]:::ext
    clipboard["<b>Буфер обмена ОС</b><br/>файлы и текст"]:::ext

    user -->|"работает"| app
    app -->|"тема, проект"| globalCfg
    app -->|"дерево, вкладки"| projectCfg
    app -->|"текст вкладок"| clipCache
    app <-->|"читает, пишет, следит"| projectFiles
    app -->|"показать файл"| fm
    app <-->|"копировать, вставить"| clipboard
```

| Связь | Когда | Технология |
|---|---|---|
| приложение → глобальный конфиг | читает при старте; пишет при смене темы и открытии проекта. Содержит тему, последний проект, окно | `kotlinx.serialization`, атомарная запись |
| приложение → конфиг проекта | читает при открытии проекта; пишет с задержкой (debounce). Содержит дерево (корень и раскрытые папки), открытые вкладки, размеры панелей | `kotlinx.serialization`, атомарная запись |
| приложение → кэш вкладок | пишет при выходе, читает при открытии, удаляет при закрытии вкладки | `java.nio.file` |
| приложение ↔ файлы проекта | читает, пишет, переносит, следит за изменениями | `java.nio.file`, `WatchService` |
| приложение → файловый менеджер | показать файл | `ProcessBuilder` |
| приложение ↔ буфер обмена | копировать, вырезать, вставить | AWT `Clipboard` |

Папка глобального конфига: Linux — `~/.config/AisDme`, Windows — `%APPDATA%/AisDme`.

## 3. Component

Приложение показано четырьмя диаграммами: события через шину, прямые вызовы, конфиги и состояние, файлы и платформа. Одна диаграмма со всеми компонентами получилась бы перегруженной.

Чего на диаграммах нет:

- Compose UI: экраны получают контроллеры через `LocalAppController`.
- Стрелок от `AppController` к каждому контроллеру: он создаёт их все и передаёт им шину. На диаграмме шины показана только его связь с самой шиной.
- `UiController` — абстрактный предок контроллеров (корутины и поток сообщений), отдельным компонентом не показан. `GlobalMessages` подписывается на сообщения всех контроллеров (`collectFrom`) и показывает их в `ErrorOverlay`.
- `MoveController` (состояние drag & drop) связан только с UI и нигде не показан.

### 3.1 Контроллеры и события

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 50, "rankSpacing": 100}}}%%
flowchart TB
    classDef comp fill:#438dd5,stroke:#2e6295,color:#fff
    classDef bus fill:#b45309,stroke:#7c3a06,color:#fff

    subgraph app["Desktop-приложение"]
        appC["<b>AppController</b><br/>корень композиции"]:::comp
        filesC["<b>FilesController</b><br/>файловые операции"]:::comp
        watcher["<b>TreeReloadWatcher</b><br/>изменения на диске"]:::comp
        bus["<b>AppEventBus</b><br/>MutableSharedFlow"]:::bus
        projectC["<b>ProjectController</b><br/>дерево файлов"]:::comp
        editorC["<b>EditorController</b><br/>вкладки редактора"]:::comp
    end
    style app fill:transparent,stroke:#888,stroke-dasharray:6 4

    appC -->|"создаёт"| bus
    filesC -->|"MoveFile, DeleteFile,<br/>DeleteDir"| bus
    watcher -->|"RebuilDirRiff"| bus
    projectC <-->|"публикует: FileOpened<br/>слушает: RebuilDirRiff,<br/>ShowFileInTree"| bus
    editorC <-->|"публикует: ShowFileInTree<br/>слушает: FileOpened, MoveFile,<br/>DeleteFile, DeleteDir"| bus
```

| Событие | Кто публикует | Кто слушает | Зачем |
|---|---|---|---|
| `FileOpened` | `ProjectController` | `EditorController` | двойной клик по файлу: открыть вкладку |
| `ShowFileInTree` | `EditorController` | `ProjectController` | показать файл вкладки в дереве |
| `RebuilDirRiff` | `TreeReloadWatcher` | `ProjectController` | папка изменилась на диске: перечитать |
| `MoveFile`, `DeleteFile`, `DeleteDir` | `FilesController` | `EditorController` | файл переехал или удалён: обновить или закрыть вкладки |

### 3.2 Прямые вызовы

Связи, которые идут не через шину. Пунктир — вызов метода.

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 45, "rankSpacing": 130}}}%%
flowchart LR
    classDef comp fill:#438dd5,stroke:#2e6295,color:#fff

    subgraph app["Desktop-приложение"]
        treeActions["<b>TreeActions</b><br/>операции над выделенным"]:::comp
        dialogsC["<b>DialogsController</b><br/>диалоги"]:::comp
        projectC["<b>ProjectController</b><br/>дерево файлов"]:::comp
        editorC["<b>EditorController</b><br/>вкладки редактора"]:::comp
        filesC["<b>FilesController</b><br/>файловые операции"]:::comp
        watcher["<b>TreeReloadWatcher</b><br/>изменения на диске"]:::comp
    end
    style app fill:transparent,stroke:#888,stroke-dasharray:6 4

    treeActions -.->|"getSelectedNodes, markCut"| projectC
    treeActions -.->|"openNodes"| editorC
    treeActions -.->|"copyToClipboard"| filesC
    dialogsC -.->|"getSelectedNodes"| projectC
    projectC -.->|"dirsLoaded"| watcher
```

| Вызов | Зачем |
|---|---|
| `TreeActions` → `ProjectController` | выделенные узлы; пометка «вырезано» |
| `TreeActions` → `EditorController` | открыть выделенные файлы в левой или правой половине |
| `TreeActions` → `FilesController` | положить выделенные файлы в буфер обмена |
| `DialogsController` → `ProjectController` | выделенные узлы для диалога (функция из `AppController`) |
| `ProjectController` → `TreeReloadWatcher` | `dirsLoaded`: прочитанные папки дерева, на Linux watcher следит только за ними |

Связка `dirsLoaded` сделана в `AppController`, потому что шина при переполнении теряла события.

### 3.3 Конфиги и состояние

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 45, "rankSpacing": 120}}}%%
flowchart LR
    classDef comp fill:#438dd5,stroke:#2e6295,color:#fff
    classDef db fill:#2b7a4b,stroke:#1d5434,color:#fff

    subgraph app["Desktop-приложение"]
        editorC["<b>EditorController</b><br/>вкладки редактора"]:::comp
        projectC["<b>ProjectController</b><br/>дерево файлов"]:::comp
        themeC["<b>ThemeController</b><br/>тема"]:::comp
        editorRep["<b>EditorStateRepository</b><br/>снимок вкладок"]:::comp
        treeRep["<b>TreeStateRepository</b><br/>снимок дерева"]:::comp
        configC["<b>ConfigController</b><br/>автосохранение"]:::comp
        store["<b>ConfigStore</b><br/>kotlinx.serialization"]:::comp
        platform["<b>Platform</b><br/>папка конфигов ОС"]:::comp
    end
    style app fill:transparent,stroke:#888,stroke-dasharray:6 4

    projectCfg[("<b>Конфиг проекта</b><br/>.aisdme/config.json")]:::db
    globalCfg[("<b>Глобальный конфиг</b><br/>config.json в папке ОС")]:::db

    editorC -->|"снимок"| editorRep
    projectC -->|"снимок"| treeRep
    configC -->|"читает, debounce 4 с"| editorRep
    configC -->|"читает, debounce 2 с"| treeRep
    configC -->|"update"| store
    themeC -->|"update"| store
    store -->|"JSON"| projectCfg
    store -->|"JSON"| globalCfg
    store -.->|"configRoot"| platform
```

Репозитории — `MutableStateFlow`, в них пишут присваиванием `value`. `ConfigStore` пишет JSON атомарно, под `Mutex`, с кэшем.

### 3.4 Файлы, буфер обмена и платформа

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 45, "rankSpacing": 120}}}%%
flowchart LR
    classDef comp fill:#438dd5,stroke:#2e6295,color:#fff
    classDef ext fill:#6b6b6b,stroke:#4a4a4a,color:#fff

    subgraph app["Desktop-приложение"]
        filesC["<b>FilesController</b><br/>файловые операции"]:::comp
        executor["<b>FilesExecutor</b><br/>java.nio.file"]:::comp
        transferable["<b>FileTransferable</b><br/>AWT datatransfer"]:::comp
        watcher["<b>TreeReloadWatcher</b><br/>WatchService"]:::comp
        platform["<b>Platform</b><br/>Windows, Linux"]:::comp
    end
    style app fill:transparent,stroke:#888,stroke-dasharray:6 4

    fs["<b>Файловая система</b><br/>файлы проекта"]:::ext
    clipboard["<b>Буфер обмена ОС</b><br/>файлы и текст"]:::ext
    fm["<b>Файловый менеджер ОС</b><br/>Explorer, Dolphin и др."]:::ext

    filesC -->|"операции над Path"| executor
    executor -->|"читает, переносит, удаляет"| fs
    executor -->|"создаёт содержимое"| transferable
    executor <-->|"файлы"| clipboard
    transferable -.->|"формат «вырезано»"| platform
    executor -.->|"isClipboardCut"| platform
    watcher -->|"registerWatch"| platform
    platform -->|"WatchService"| fs
    platform -->|"showInFileManager"| fm
```

`Platform` — интерфейс `IPlatform` с реализациями `WindowsPlatform` и `LinuxPlatform`. На Windows `WatchService` регистрирует корень с `FILE_TREE`.

## 4. Dynamic

### 4.1 Открытие файла из дерева

Двойной клик по файлу открывает вкладку, а состояние редактора попадает в конфиг проекта.

```mermaid
sequenceDiagram
    actor user as Пользователь
    participant projectC as ProjectController
    participant bus as AppEventBus
    participant editorC as EditorController
    participant tab as TabCashFileText
    participant fs as Файловая система
    participant editorRep as EditorStateRepository
    participant configC as ConfigController
    participant cfg as Конфиг проекта

    user->>projectC: двойной клик по файлу
    projectC->>bus: FileOpened
    bus->>editorC: FileOpened (UI-поток)
    editorC->>tab: создать вкладку, открыть файл
    tab->>fs: прочитать текст
    editorC->>editorRep: снимок вкладок
    configC->>editorRep: прочитать снимок (debounce 4 с)
    configC->>cfg: записать конфиг проекта (ConfigStore)
```

### 4.2 Изменение на диске извне

Файл создали в файловом менеджере: дерево обновляется без действий пользователя. На Windows watcher следит за всем деревом одним ключом, на Linux — только за корнем и папками, прочитанными в дерево.

```mermaid
sequenceDiagram
    participant fs as Файловая система
    participant watcher as TreeReloadWatcher
    participant bus as AppEventBus
    participant projectC as ProjectController
    participant treeState as ProjectTreeState
    participant ui as UI

    fs->>watcher: ENTRY_CREATE, ENTRY_DELETE (WatchService)
    Note over watcher: собирает события 200 мс
    watcher->>bus: RebuilDirRiff для каждой папки, которая ещё есть
    bus->>projectC: RebuilDirRiff
    projectC->>treeState: rebuildDirDiff
    treeState->>fs: перечитать прочитанные папки (listFiles)
    treeState->>ui: новые узлы (на UI-потоке)
```

## Расхождения с решениями

- **«Контроллеры общаются через шину».** В [docs/ai/architecture.md](ai/architecture.md) это сказано без оговорок. По коду шина — основной, но не единственный путь:
  - прочитанные папки дерева идут в watcher напрямую (`ProjectTreeState.onDirsLoaded` → `TreeReloadWatcher.dirsLoaded`, связка в `AppController`). Это решение записано в [decisions.md](ai/decisions.md), «Ленивая загрузка дерева проекта»: шина при переполнении теряла события;
  - `TreeActions` и `DialogsController` вызывают контроллеры напрямую.
- **`UiController`** в issue [#35](https://github.com/efimov-r/aisdme/issues/35) назван контроллером. По коду это абстрактный предок: корутины и поток сообщений для всех контроллеров. Отдельным компонентом он не нарисован.
- **Сообщения об ошибках** на схемах не показаны: каждый контроллер отдаёт их в `GlobalMessages`, а он показывает в `ErrorOverlay`. Это сквозная связь, она перегрузила бы диаграммы.
