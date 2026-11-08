# Ловушки

На чём уже ошибались и как правильно. Новые записи — сверху.

## Compose Desktop показывает ошибку системным диалогом и закрывает окно
- **Что случилось:** `IllegalArgumentException` в `drawText` (`LineNumbers`) у слишком узкой половины редактора: поверх приложения всплыло системное окно «Error» с кнопкой OK, после неё окно закрывалось. В пульте это выглядело как «зависание»: системные окна на снимок не попадают, а диалог модальный
- **Причина:** исключения кадра и событий окна Compose ловит `WindowExceptionHandler` из `LocalWindowExceptionHandlerFactory`; по умолчанию (`DefaultWindowExceptionHandlerFactory`) это `JOptionPane` и закрытие окна. Обычный `Thread.setDefaultUncaughtExceptionHandler` этот путь не перехватывает
- **Как правильно:** свой `WindowErrors` в `LocalWindowExceptionHandlerFactory` (`MainWindow`) отдаёт ошибку в `UncaughtErrors` → `ErrorOverlay`. Исключение, которое повторяется в каждом кадре (как в `LineNumbers`), оверлей показать не даёт: кадр падает раньше, чем рисуется оверлей (он последний). Такое надо чинить в причине: то, что рисуется в `drawBehind` / `Canvas`, не должно бросать ни на каких размерах (у `drawText` ширину ограничивать снизу нулём). Проверка показа ошибок — `throw текст` в пульте, сценарий `uncaught-error`

## Метки пульта: `boundsInWindow` обрезает по родителю
- **Что случилось:** `expect inside tab:… in tabs:left` проходил для вкладки, наполовину вылезшей за край панели ([#36](https://github.com/efimov-r/aisdme/issues/36))
- **Причина:** `LayoutCoordinates.boundsInWindow()` возвращает видимую часть, обрезанную по границам родителей. Запомненный при размещении прямоугольник ещё и устаревает, когда `LazyRow` убирает элемент из раскладки
- **Как правильно:** `DebugTag` хранит `LayoutCoordinates` и при запросе берёт `Rect(positionInWindow(), size)`; отсоединённые узлы пропускает. Разделитель в `tap A within B` — не ` in `: он ломает `tap menu:Open in Right`

## Синтетические события не доходят до окна Compose
- **Что случилось:** пульт отладки слал `MouseEvent` в найденный `SkiaLayer`: команды выполнялись без ошибок, а окно не реагировало
- **Причина:** слушатели мыши и клавиатуры висят на вложенном компоненте (`org.jetbrains.skiko.SkiaLayer$1`), а у самого `SkiaLayer` их нет. Нашлось командой `dump`
- **Как правильно:** слать события в `SwingUtilities.getDeepestComponentAt(layer, …)` (`DebugInput.target`)

## Сверка вкладки с диском держит `saveMutex` и ждёт ui-поток
- **Что случилось:** риск зависания при выходе ([#9](https://github.com/efimov-r/aisdme/issues/9)): `syncWithDisk` под `saveMutex` переключается на `Dispatchers.Swing`, а `disposeBeforeBlocked` на EDT (через `runBlocking`) ждёт тот же `saveMutex`
- **Как правильно:** в `disposeBeforeBlocked` ждать мьютекс с `withTimeoutOrNull`, не бесконечно. Новый код под `saveMutex` не должен ждать ui-поток дольше, чем нужно; в `disposeBeforeBlocked` по-прежнему не звать `writeStatus`

## Detekt: `TooManyFunctions` в `TabCashFileText`
- **Что случилось:** после сверки с диском в классе стало больше 10 функций, detekt упал ([#9](https://github.com/efimov-r/aisdme/issues/9))
- **Как правильно:** порог срабатывает при 11 функциях (`>=`). Чистые помощники — приватные функции файла вне класса; три действия плашки — одна `resolve(ExternalChoice)`. Следующую функцию в этот класс не добавлять: выносить в отдельный класс

## Тест опрашивает `derivedStateOf` из потока теста
- **Что случилось:** `EditorControllerTest.derivedStateSeesRightToLeftHalf` один раз упал в CI на Linux по `TimeoutCancellationException` (прогон на master после слияния PR [#38](https://github.com/efimov-r/aisdme/pull/38)); в PR-прогоне того же кода прошёл
- **Причина (гипотеза, не воспроизведена):** `waitUntil` читал `derivedStateOf` из потока теста, а `leftTabs` меняется на ui-потоке. Пересчёт мог гоняться с записью и закэшировать старое значение навсегда
- **Как правильно:** state читать там же, где он пишется, на ui-потоке: `waitUntil { onUi { value } … }`. Не воспроизведётся — запись в «Известные нестабильные тесты» не нужна, но при повторе падения гипотеза неверна

## Тест с вкладкой редактора падает при очистке `@TempDir` на Windows
- **Что случилось:** `EditorControllerTest.newTabIdAfterSnapshotIds` падал в 3 прогонах из 4: `Failed to delete temp directory ... b.asm: файл занят другим процессом`; сами проверки проходили ([#39](https://github.com/efimov-r/aisdme/issues/39))
- **Причина:** вкладка читает файл в фоне (в debug перед этим `delay(300)`), а тест кончается раньше; `dispose()` не ждёт блокирующее `readText`. На Linux открытый файл удалить можно, поэтому CI ошибку не видел. Причина подтверждена только тем, что ожидание загрузки убрало падение (3 прогона подряд)
- **Как правильно:** перед `controller.dispose()` дождаться, пока вкладки выйдут из `WriteStatus.LOAD` (`EditorControllerTest.dispose`)

## `snapshotFlow` в тесте не видит изменение state
- **Что случилось:** `ProjectController` подгружает раскрытые папки через `snapshotFlow`; тест выставил `expanded = true`, а папка не читалась
- **Причина:** вне Compose нет `GlobalSnapshotManager`, который рассылает уведомления о записи в глобальный снимок
- **Как правильно:** в тесте после записи в state вызвать `Snapshot.sendApplyNotifications()` (`ProjectControllerTest.expandedDirIsLoadedAndSaved`)

## detekt: свой `excludes` затирает стандартный
- **Что случилось:** после `excludes: ['**/theme/**']` у `MagicNumber` detekt упал на 41 числе в тестах ([#11](https://github.com/efimov-r/aisdme/issues/11))
- **Причина:** `excludes` в `detekt.yml` не дополняет, а заменяет стандартный список правила; у `MagicNumber` в нём тестовые папки и `*.kts`
- **Как правильно:** скопировать стандартный список правила из `default-detekt-config.yml` своей версии detekt и дописать своё, как у `MagicNumber` в `detekt.yml`

## Тест читает половину редактора до отложенного объединения
- **Что случилось:** `TreeActionsTest.openSelectedInRightHalf` прошёл на Windows и упал в CI на Linux: `NullPointerException` на `rightTabs.activeTabId!!` (PR [#37](https://github.com/efimov-r/aisdme/pull/37))
- **Причина:** левая половина была пустой, файл открывался справа; `mergeHalvesLater` ставит перенос правой в левую в очередь ui-потока. На Linux очередь успевала отработать до чтения, на Windows — нет
- **Как правильно:** после операции с половинами, перед чтением `leftTabs` / `rightTabs`, дождаться очереди: `runBlocking(Dispatchers.Swing) {}`. Правая половина остаётся правой, только если левая не пустая

## Половины редактора нельзя объединять посреди операции
- **Что случилось:** при переносе `rightToLeftHalf` из composition в контроллер объединение сначала делалось сразу, в `onChanged` закрытия вкладки. «Split and Move Other Half» терял вкладку
- **Причина:** UI держит ссылки на половины и меняет их по очереди (`half.closeTab`, затем `oppositeHalf.activateTab`). После мгновенного объединения `oppositeHalf` — уже отвязанный объект; так же `closeTabs` не закрыл бы вкладку, открытую в обеих половинах
- **Как правильно:** объединять после операции целиком — `mergeHalvesLater` через `Dispatchers.Swing`. Тест — `EditorControllerTest.splitAndMoveLastLeftTabKeepsTab`

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
- **Причина:** в `TabsBar` `derivedStateOf` читал обычные поля: `EditorController.leftTabs` / `rightTabs` (обычные `var`, `rightToLeftHalf` заменяет объекты) и кэш вкладок в `MutableMap` (`getTab`). Плюс `remember` без ключа `half` — после замены половины вычисление смотрело в список старой ([#27](https://github.com/efimov-r/aisdme/issues/27))
- **Как правильно:** всё, от чего зависит `derivedStateOf`, держать в `mutableStateOf`, `mutableStateListOf`, `mutableStateMapOf`; параметры composable, которые читаются внутри, — ключи `remember(x)`. Тесты — `EditorControllerTest.derivedStateSees*`

## Различия Linux и Windows
- **Что случилось:** буфер обмена, пути конфигов и открытие файла в файловом менеджере работают в ОС по-разному
- **Как правильно:** всё, что зависит от ОС, — только в `platform/`, в обеих реализациях: `WindowsPlatform` и `LinuxPlatform`. Вне `platform/` `os.name` не читать
