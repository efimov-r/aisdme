# Ловушки

На чём уже ошибались и как правильно. Новые записи — сверху.

## Тест класса, у которого пакет не совпадает с папкой
- **Что случилось:** `SnapshotEditorTest` в `src/test/kotlin/configs/snapshots/` (как `SnapshotEditor`) уронил detekt: `InvalidPackageDeclaration`
- **Причина:** у `SnapshotEditor` пакет `configs.dataclass.snapshots`; нарушение в `src/main` — в baseline, новые файлы detekt проверяет
- **Как правильно:** тест такого класса класть в папку по пакету (`src/test/kotlin/configs/dataclass/snapshots/`), пакет — как у класса

## Push отклонён: `fatal error in commit_refs_rules`
- **Что случилось:** push новой ветки отклонялся (2026-11-05), хотя ruleset `master` действует только на основную ветку. Выключить ruleset в настройках не получалось: после Save он снова был Active
- **Причина:** сбой ruleset на стороне GitHub; точная причина неизвестна. GitHub в это время работал без инцидентов
- **Как правильно:** удалить ruleset, сделать push, создать ruleset заново с теми же правилами (список — в `AGENTS.md`, «CI»); сверить: `gh api repos/efimov-r/aisdme/rulesets/<id>`

## Windows: переименование только регистром
- **Что случилось:** `a.asm` → `A.asm` молча не переименовывался — и с `REPLACE_EXISTING`, и без
- **Причина:** на Windows `Files.move` для того же файла ничего не делает, а `Path.equals` не различает регистр: `src != target` — `false`
- **Как правильно:** сравнивать `toString()`, тот же файл — `Files.isSameFile`, переименовывать через `File.renameTo` (`FilesExecutor.move`). Тест — `FilesExecutorTest.moveCaseOnlyRenameKeepsFile`

## `dispose` идёт на EDT через `runBlocking`
- **Что случилось:** риск зависания при выходе
- **Причина:** `EditorController.disposeBefore` вызывается из `onDispose` на EDT и ждёт `runBlocking`; `withContext(Dispatchers.Swing)` внутри не выполнится никогда
- **Как правильно:** в `disposeBeforeBlocked` не вызывать `writeStatus` и ничего, что переключается на `Swing`

## Kover: пакет `ui` нельзя исключать из покрытия целиком
- **Что случилось:** план был — не считать покрытие в `ui/`, как UI-код
- **Причина:** в `ui/` лежит логика: контроллеры, `ProjectTreeState`, `TabCash*`, `FileNameValidator` (единственный класс с тестом на 2026-11-05)
- **Как правильно:** исключать `annotatedBy("androidx.compose.runtime.Composable")`, `*ComposableSingletons*` и `theme/` — так в `build.gradle.kts`

## detekt запускается без type resolution
- **Что случилось:** правила, которым нужны типы (например, проигнорированный результат вызова), задача `detekt` молча пропускает
- **Причина:** detekt 1.23.8 собран с Kotlin 2.0.21, у проекта 1.9.22; `detektMain` с type resolution не проверялся
- **Как правильно:** гейт — задача `detekt`. Если понадобится `detektMain` — сначала проверить, что он работает на Kotlin проекта

## Свой формат буфера обмена не виден в содержимом, полученном раньше регистрации
- **Что случилось:** пометка «вырезано» из Explorer (`Preferred DropEffect`) читается через свой `DataFlavor`, связанный с форматом ОС в `SystemFlavorMap`
- **Причина:** `Clipboard.getContents` переводит форматы ОС в `DataFlavor` в момент вызова. Если связь ещё не зарегистрирована, своего flavor в содержимом не будет
- **Как правильно:** обратиться к `FileTransferable.cutFlavor` (регистрация — в его инициализации) до `getContents`, как в `FilesExecutor.getClipboardFiles`

## Windows: `WatchService` блокирует переименование папок
- **Что случилось:** папка с подпапками не переименовывалась, `AccessDeniedException`
- **Причина:** на Windows каждая зарегистрированная в `WatchService` папка остаётся открытой, а папку с открытой подпапкой переименовать нельзя
- **Как правильно:** на Windows регистрировать только корень проекта с `ExtendedWatchEventModifier.FILE_TREE` (`WindowsPlatform.registerWatch`); новые папки не регистрировать

## Приложение закрывается, когда нет ни одного окна
- **Что случилось:** при старте без проекта сначала закрывали заставку, потом открывали `JFileChooser` из `LaunchedEffect`. После выбора папки приложение закрывалось, в логах ничего ([#2](https://github.com/efimov-r/aisdme/issues/2))
- **Причина:** предположение: `application {}` завершается, когда в композиции нет окон, а последний эффект закончился. Новое состояние после этого уже не применяется
- **Как правильно:** пока идёт работа вне Compose (модальный диалог Swing и т. п.), держать в композиции хотя бы одно окно. Окно, которое должно жить в нескольких состояниях, вызывать в одном месте, а не в разных ветках `when`: иначе оно пересоздаётся и моргает

## «Composable function call without Compose plugin» на `remember` и других `@Composable`
- **Что случилось:** IDEA 2026.2 подсвечивает вызовы `@Composable`, хотя Gradle собирает проект
- **Причина:** инспекция `ComposableCallWithoutComposePlugin` ищет плагин `org.jetbrains.kotlin.plugin.compose`, а он есть только с Kotlin 2.0. На Kotlin 1.9 компилятор Compose подключает плагин `org.jetbrains.compose`, его инспекция не видит
- **Как правильно:** отключить инспекцию (Alt+Enter → стрелка у пункта → Disable inspection). Quick-fix «добавить плагин» не применять: он добавит плагин Kotlin 2.x в проект на 1.9 и сломает сборку. Окончательно уйдёт при переходе на Kotlin 2.x

## Классы JDK (`File`, `System`) красные в IDEA
- **Что случилось:** IDE не находит классы JDK, хотя Gradle собирает проект
- **Причина:** у SDK проекта (`17`) в настройках IDEA пустой classPath: нет корней `jrt://`. SDK был зарегистрирован из JDK, который скачал Gradle toolchain (`~/.gradle/jdks/`). Нормальный Project SDK (например, `ms-17`) не спасает: из-за `jvmToolchain(17)` IDEA при синхронизации с Gradle назначает модулю SDK toolchain, а Gradle JVM в `.idea/gradle.xml` тоже ссылается на `17`
- **Как правильно:** Project Structure → SDKs: удалить SDK и добавить заново с той же папки или выбрать другой JDK 17; затем Reload Gradle. Проверка: в `jdk.table.xml` настроек IDEA у SDK есть строки `jrt://`

## Пакет не совпадает с папкой
- **Что случилось:** файлы из `configs/configs/` объявлены в пакете `non.era.aisdme.configs.dataclass`, `configs/snapshots/SnapshotTree.kt` — в `configs.dataclass.child`, `SnapshotEditor.kt` — в `configs.dataclass.snapshots`
- **Причина:** папки переименовывали, а пакеты — нет
- **Как правильно:** в новом файле брать `package` у соседних файлов той же папки; при импорте искать класс по имени, а не по пути

## `BuildConfig` не находится
- **Что случилось:** IDE подсвечивает `BuildConfig` красным
- **Причина:** его генерирует задача `generateBuildConfig` в `build/generated/buildconfig` только при сборке
- **Как правильно:** один раз собрать проект через Gradle; файл руками не создавать

## `derivedStateOf` не обновляется
- **Что случилось:** при рекомпозиции состояние в `derivedStateOf` иногда не пересчитывалось
- **Причина:** TODO: точная причина не записана. Частая причина в Compose — внутри `derivedStateOf` читается обычное поле, а не `State`: тогда пересчёта не будет
- **Как правильно:** всё, от чего зависит `derivedStateOf`, держать в `mutableStateOf` или `mutableStateListOf`. Используется в `TabsBar.kt` и `ProjectTreeView.kt`

## Различия Linux и Windows
- **Что случилось:** буфер обмена, пути конфигов и открытие файла в файловом менеджере работают в ОС по-разному
- **Как правильно:** всё, что зависит от ОС, — только в `platform/`, в обеих реализациях: `WindowsPlatform` и `LinuxPlatform`. Вне `platform/` `os.name` не читать
