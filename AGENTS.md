# AisDme

Учебная IDE для ассемблера на Kotlin и Compose for Desktop, по образцу IntelliJ IDEA. Хобби-проект и заодно изучение Compose. Сейчас есть дерево проекта с файловыми операциями, редактор текста с вкладками в двух половинах экрана, просмотр изображений, конфиги проекта и ОС, своя тема. Ассемблерной части (подсветки, сборки) пока нет.

## Стек

- **Язык и версии:** Kotlin 1.9.22, JDK 17 (`jvmToolchain(17)`), Gradle 8.14 (wrapper), Kotlin DSL
- **Фреймворки и библиотеки:** Compose for Desktop 1.6.10 (`org.jetbrains.compose`), `kotlinx-coroutines-swing`, `kotlinx-serialization-json` 1.6.3
- **Платформы:** Linux и Windows. Различия — буфер обмена, пути конфигов, отслеживание диска, открытие в файловом менеджере; весь такой код — в `platform/`. Разрабатывался в Manjaro Linux
- **Тесты:** JUnit 5 + `kotlin.test`, в `src/test/kotlin/`

## Сборка, запуск, тесты

```
gradlew run               # запуск, debug-сборка (BuildConfig.DEBUG = true)
gradlew run -Prelease     # запуск, release-сборка (BuildConfig.DEBUG = false)
gradlew build             # сборка + check: тесты, detekt, порог покрытия
gradlew test              # модульные тесты
gradlew detekt            # статический анализ, отчёт — build/reports/detekt/
gradlew koverLog          # текущее покрытие; koverHtmlReport — отчёт в build/reports/kover/
```

На Windows `gradlew build` и `gradlew run` работают с JDK 17, если задан `JAVA_HOME` (проверено 2026-11-05).

- `BuildConfig` генерирует задача `generateBuildConfig` в `build/generated/buildconfig`, перед каждой компиляцией Kotlin. Руками его не создавать.
- Главный класс — `non.era.aisdme.MainKt`.
- CI — `.github/workflows/ci.yml`: `gradlew build` на Linux и Windows при push в `master` и в каждом PR. Actions закреплены по SHA, рядом — версия. Упавший прогон: `gh run view --log-failed`, отчёты — artifact `reports-<ОС>`.
- `master` защищён ruleset `master`: только через PR, обязательные проверки `build (ubuntu-latest)` и `build (windows-latest)`, ветка должна быть свежей. Переименовал job или ОС в матрице — обновить проверки в ruleset (Settings → Rules), иначе PR будет ждать проверку, которой нет.

### Quality gates

- **Перед каждым коммитом** с кодом — `gradlew build`, он должен пройти. Не прошёл — не коммитить.
- **detekt:** конфиг — `config/detekt/detekt.yml`, там только отличия от стандарта под [docs/ai/style.md](docs/ai/style.md). Старые нарушения — в `config/detekt/baseline.xml`; новые роняют сборку.
- **Kover:** порог покрытия строк — `minBound` в `build.gradle.kts`, без `@Composable` и `theme/`. Порог только растёт: после новых тестов — поднять до нового значения, округлённого вниз.
- **Нельзя без решения пользователя:** отключать или ослаблять правила detekt, пересоздавать baseline (`gradlew detektBaseline`), добавлять `@Suppress`, понижать порог Kover. Исключение — узкое (файл и правило) и с причиной в комментарии.

## Структура кода

Один Gradle-модуль, пакет `non.era.aisdme`, код в `src/main/kotlin/`. Пакет не везде совпадает с папкой: [docs/ai/gotchas.md](docs/ai/gotchas.md).

- `Main.kt` — `main()`, корневой Compose-экран `AisDmeContent`
- `di/` — `AppController`: корень композиции, сам создаёт и связывает контроллеры, доступен через `LocalAppController`; `buildAppController` — `remember` над ним
- `bus/` — `AppEventBus` на `MutableSharedFlow`: события между контроллерами
- `configs/` — `ConfigController`, `GlobalConfig`, `store/` (`ConfigStore`, `ConfigPaths`), `configs/` (`AppConfig`, `ProjectConfig`), `snapshots/` (сохранённое состояние редактора и дерева), `repositories/`
- `files/` — `TypeFile`, `work/`: `FilesController`, `FilesExecutor`, `FileTransferable` (буфер обмена)
- `platform/` — всё, что зависит от ОС: папка конфигов, отслеживание диска, показ в файловом менеджере. `IPlatform`, `WindowsPlatform`, `LinuxPlatform`, выбор — `Platform.current`
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
- `src/test/kotlin/` — модульные тесты: папки и пакеты — как у тестируемого класса, имя — `<Класс>Test`
- `src/main/resources/` — `icons/` (SVG в стиле IntelliJ, светлая и тёмная версии), `logos/`

Подробнее об устройстве — [docs/ai/architecture.md](docs/ai/architecture.md).

## Правила работы

- Важное держать не в чате, а в файлах памяти ниже. Новые ловушки, баги и решения записывать сразу, по ходу работы.
- Репозиторий считается публичным: в памяти проекта не должно быть ни секретов, ни личной информации.
- Этот файл — до 200 строк; подробности — в `docs/ai/`.
- Файлы памяти — на русском.
- **Коммиты:** агент коммитит сам, когда решена одна логическая задача и код в рабочем состоянии. Посреди задачи не коммитить. Коммит — только если в нём есть код: правки одной документации и памяти (`AGENTS.md`, `docs/ai/`) отдельно не коммитить, они лежат локально и входят в следующий коммит с кодом. Сообщение — в стиле [docs/ai/style.md](docs/ai/style.md), в конце строка `Co-Authored-By`. В коммит — только свои изменения; чужие файлы, например созданные IDE в `.idea/`, не добавлять. После коммита — коротко сообщить, что в нём. Push делает только пользователь.
- **Ветки:** основная ветка — `master`, `dev` в неё слита. Каждая новая задача — в новой ветке от свежего `master`, агент создаёт её сам до первой правки кода: `git fetch --prune`, затем `git switch --no-track -c issue-<N>-<кратко> origin/master`. Переключать ветки агент может сам, без просьбы; других веток без просьбы не создавать. Локальные ветки, которые слиты в `master` и удалены на GitHub, удалять всегда, без вопроса: `git fetch --prune`, затем `git branch -d` для веток с `gone` в `git branch -vv --merged master`. Если стоишь на такой ветке — сначала `git switch master` и `git pull --rebase`.
- **Pull request:** после push пользователя PR создаёт агент: `gh pr create --base master`. Описание — по навыку `pr`, если он есть; в описании — `Closes #<N>`, чтобы issue закрылась при слиянии; в конце — строка `Generated with Claude Code`. Слияние делает пользователь, после зелёного CI.
- **Статус задачи:** взял задачу (в том числе когда пользователь просит план по ней) — вместе с веткой повесить на issue метку `in progress` (`gh issue edit <N> --add-label "in progress"`) и записать её в «В работе» в [docs/ai/todo.md](docs/ai/todo.md). Статус назад не откатывается: задача из «в работе» уходит только в решённые. Метку снимать (`--remove-label "in progress"`) и убирать задачу из `todo.md` — только когда issue закрыта. Если PR закрыл лишь часть issue, метка остаётся, задача — в «В работе». GitHub Projects не используются: fine-grained токен `gh` к ним доступа не даёт.

- **Диаграммы архитектуры:** [docs/architecture.md](docs/architecture.md) показывает текущий код. Меняешь структуру — обнови диаграммы в том же PR: новый контроллер или компонент, новая связь (шина, прямой вызов), новое хранилище или конфиг, новая внешняя система, новый сценарий в Dynamic. Правки внутри компонента без новых связей диаграмм не требуют.

## Память проекта

| Файл | Что в нём | Когда читать |
|---|---|---|
| [docs/ai/todo.md](docs/ai/todo.md) | что в работе и что дальше | в начале сессии |
| [docs/ai/architecture.md](docs/ai/architecture.md) | как устроено приложение | перед изменениями в незнакомой части кода |
| [docs/ai/bugs.md](docs/ai/bugs.md) | известные баги и их статус | при ошибке — до того, как чинить |
| [docs/ai/gotchas.md](docs/ai/gotchas.md) | ловушки: на чём уже ошибались | перед правкой конфигов, зависимостей, сборки |
| [docs/ai/decisions.md](docs/ai/decisions.md) | решения и почему | перед рефакторингом и «улучшениями» |
| [docs/ai/style.md](docs/ai/style.md) | стиль кода и текстов | перед написанием кода и текстов |
| [docs/architecture.md](docs/architecture.md) | диаграммы C4 (Mermaid) | при изменении структуры приложения: обновить |
