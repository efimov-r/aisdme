# Ловушки

На чём уже ошибались и как правильно. Новые записи — сверху.

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
- **Как правильно:** при правке `ConfigPaths`, `FileTransferable`, `ClipboardHelper`, `FileDolphinOpener` проверять обе ветки кода — для Linux и для Windows
