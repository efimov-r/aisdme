# AisDme

Учебная IDE для ассемблера NASM на Kotlin и Compose for Desktop, по образцу IntelliJ IDEA. Хобби-проект и заодно изучение Compose. Сейчас есть дерево проекта с файловыми операциями, редактор текста с вкладками в двух половинах экрана, просмотр изображений, конфиги проекта и ОС, своя тема. Ассемблерной части (подсветки, сборки NASM) пока нет.

## Стек

- **Язык и версии:** Kotlin 1.9.22, JDK 17 (`jvmToolchain(17)`), Gradle 8.14 (wrapper), Kotlin DSL
- **Фреймворки и библиотеки:** Compose for Desktop 1.6.10 (`org.jetbrains.compose`), `kotlinx-coroutines-swing`, `kotlinx-serialization-json` 1.6.3
- **Платформы:** Linux и Windows: буфер обмена, пути конфигов, открытие в файловом менеджере. Разрабатывался в Manjaro Linux
- **Тестов нет:** папки `src/test` нет

## Сборка, запуск, тесты

```
gradlew run               # запуск, debug-сборка (BuildConfig.DEBUG = true)
gradlew run -Prelease     # запуск, release-сборка (BuildConfig.DEBUG = false)
gradlew build             # сборка
```

На Windows `gradlew build` и `gradlew run` работают с JDK 17, если задан `JAVA_HOME` (проверено 2026-11-05).

- `BuildConfig` генерирует задача `generateBuildConfig` в `build/generated/buildconfig`, перед каждой компиляцией Kotlin. Руками его не создавать.
- Главный класс — `non.era.aisdme.MainKt`.

## Структура кода

Один Gradle-модуль, пакет `non.era.aisdme`, код в `src/main/kotlin/`. Пакет не везде совпадает с папкой: [docs/ai/gotchas.md](docs/ai/gotchas.md).

- `Main.kt` — `main()`, корневой Compose-экран `AisDmeContent`
- `di/` — `AppController` (фасад над всеми контроллерами, доступен через `LocalAppController`) и `AppControllerBuilder`, где контроллеры собираются вручную
- `bus/` — `AppEventBus` на `MutableSharedFlow`: события между контроллерами
- `configs/` — `ConfigController`, `GlobalConfig`, `store/` (`ConfigStore`, `ConfigPaths`), `configs/` (`AppConfig`, `ProjectConfig`), `snapshots/` (сохранённое состояние редактора и дерева), `repositories/`
- `files/` — `TypeFile`, `work/`: `FilesController`, `FilesExecutor`, `FileTransferable` (буфер обмена), `FileDolphinOpener` (показать в файловом менеджере)
- `platform/` — всё, что зависит от ОС: `IPlatform`, `WindowsPlatform`, `LinuxPlatform`, выбор — `Platform.current`
- `settings/` — `Hotkeys`
- `theme/` — `AisDmeTheme`, `AisDmeColors`, `AisDmeShapes`, `AisDmeSpacing`
- `ui/editor/` — `EditorController`, `HalfEditorTabs`, `TabsBar`, `EditField`; `halfview/` — содержимое вкладок; `halfviewmodel/` — состояние вкладок: `TabCash` → `TabCashFile` → `TabCashFileText`, `TabCashFileImg`, а также `TabCashPaste`
- `ui/project/` — `ProjectController`, `ProjectTreeState`, узлы `data/ProjectNode`, `view/` — дерево
- `ui/reload/` — `TreeReloadWatcher`: следит за изменениями на диске
- `ui/move/` — `MoveController`: drag & drop
- `ui/components/` — контекстные меню, диалоги, свои скроллбары, `ResizableRow` и `ResizableColumn`, `LineNumbers`, `ImageViewer`, `SvgIcon`
- `ui/errors/` — глобальные сообщения, `ErrorOverlay`
- `ui/win/` — окно загрузки и главное окно, сохранение размеров и положения окна
- `utils/` — `Log`, `ClipboardHelper`, `PathHelper`
- `src/main/resources/` — `icons/` (SVG в стиле IntelliJ, светлая и тёмная версии), `logos/`

Подробнее об устройстве — [docs/ai/architecture.md](docs/ai/architecture.md).

## Правила работы

- Важное держать не в чате, а в файлах памяти ниже. Новые ловушки, баги и решения записывать сразу, по ходу работы.
- Репозиторий считается публичным: в памяти проекта не должно быть ни секретов, ни личной информации.
- Этот файл — до 200 строк; подробности — в `docs/ai/`.
- Файлы памяти — на русском.
- **Коммиты:** агент коммитит сам, когда решена одна логическая задача и код в рабочем состоянии. Посреди задачи не коммитить. Сообщение — в стиле [docs/ai/style.md](docs/ai/style.md), в конце строка `Co-Authored-By`. В коммит — только свои изменения; чужие файлы, например созданные IDE в `.idea/`, не добавлять. После коммита — коротко сообщить, что в нём. Push делает только пользователь.
- **Ветки:** основная ветка — `master`, `dev` в неё слита. Ветки не создавать и не переключать без просьбы.

## Память проекта

| Файл | Что в нём | Когда читать |
|---|---|---|
| [docs/ai/todo.md](docs/ai/todo.md) | что в работе и что дальше | в начале сессии |
| [docs/ai/architecture.md](docs/ai/architecture.md) | как устроено приложение | перед изменениями в незнакомой части кода |
| [docs/ai/bugs.md](docs/ai/bugs.md) | известные баги и их статус | при ошибке — до того, как чинить |
| [docs/ai/gotchas.md](docs/ai/gotchas.md) | ловушки: на чём уже ошибались | перед правкой конфигов, зависимостей, сборки |
| [docs/ai/decisions.md](docs/ai/decisions.md) | решения и почему | перед рефакторингом и «улучшениями» |
| [docs/ai/style.md](docs/ai/style.md) | стиль кода и текстов | перед написанием кода и текстов |
