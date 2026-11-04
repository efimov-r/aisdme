# Известные баги

При ошибке сначала искать здесь. Открытые — подробно, решённые — одной строкой.

## Открытые

- нет

## Решённые

<!-- - YYYY-MM-DD — что было — как решили -->
- 2026-11-05 — падение с `NullPointerException` при запуске без последнего проекта ([#2](https://github.com/efimov-r/aisdme/issues/2)) — выбор папки через `JFileChooser` поверх заставки (`AppStartState.ChooseProject`, `ProjectChooserHelper`); ловушка с закрытием приложения — [gotchas.md](gotchas.md)
