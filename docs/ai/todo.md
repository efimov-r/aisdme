# Задачи

Что в работе и что дальше. Выполненное удаляется — история есть в git. Подробности задач — в issues на GitHub.

## В работе

- [ ] Баги из разбора кода — [#26](https://github.com/efimov-r/aisdme/issues/26). Исправлены потеря данных (PR [#32](https://github.com/efimov-r/aisdme/pull/32)) и конфиги (PR [#33](https://github.com/efimov-r/aisdme/pull/33)). Остались потоки, watcher и мелочи — делаются в ветке `issue-26-threads-watcher-perf` одним PR вместе с #3 и #28
- [ ] Автоскролл при перетаскивании не останавливается в конце прокрутки — [#3](https://github.com/efimov-r/aisdme/issues/3)
- [ ] Производительность: дерево проекта, отслеживание диска, рекомпозиция — [#28](https://github.com/efimov-r/aisdme/issues/28)

## Проверить на Linux

Код для Linux менялся в [#12](https://github.com/efimov-r/aisdme/issues/12) (PR [#29](https://github.com/efimov-r/aisdme/pull/29)) и проверен только по коду. Проверить на Manjaro (KDE), когда будет возможность:

- [ ] `gradlew run` запускается, подхватывается старый конфиг `~/.config/AisDme/config.json` (последний проект, тема, окно)
- [ ] Дерево обновляется, если в Dolphin создать, переименовать и удалить файл и папку на 2–3 уровня вглубь, в том числе внутри только что созданной извне папки
- [ ] В меню дерева пункт называется «Show in Dolphin»; он и Alt+E на файле и на папке открывают Dolphin с выделенным элементом
- [ ] Вырезать в Dolphin → вставить в дереве: оригинал исчезает. Вырезать в дереве → вставить в Dolphin: файл исчезает из проекта. Копирование в обе стороны по-прежнему копирует
- [ ] Вставка папки в её подпапку: ошибка в диалоге Paste, кнопка OK неактивна

Ленивая загрузка дерева и watcher ([#28](https://github.com/efimov-r/aisdme/issues/28), ветка `issue-26-threads-watcher-perf`): на Linux следим только за прочитанными папками, проверено только тестами:

- [ ] Раскрыть свёрнутую папку, затем в Dolphin создать в ней файл — появляется в дереве
- [ ] В Dolphin создать файл в папке, которую ещё ни разу не раскрывали, потом раскрыть её — файл виден
- [ ] Переименовать в Dolphin раскрытую папку, раскрыть её под новым именем, создать в ней файл — появляется в дереве
- [ ] Кнопка «Selected Opened File» над деревом для файла из глубоко свёрнутой папки — папки раскрываются, файл выделен
- [ ] «Expand selected» на папке с вложенными — раскрывается всё; создание файла в глубокой папке видно в дереве
- [ ] Набор текста в редакторе не вызывает `rebuildDirDiff` в логе (нет `ENTRY_MODIFY`)

## Дальше

Главное — быстро редактировать большие текстовые файлы, в том числе вставлять в середину, и отменять ввод. Решение — [decisions.md](decisions.md), «Piece Table + LineIndex + Undo». Эпик — [#4](https://github.com/efimov-r/aisdme/issues/4).

- [ ] Piece Table вместо текста в `TextFieldValue` — [#5](https://github.com/efimov-r/aisdme/issues/5)
- [ ] LineIndex — [#6](https://github.com/efimov-r/aisdme/issues/6)
- [ ] Undo / Redo поверх Piece Table — [#7](https://github.com/efimov-r/aisdme/issues/7)
- [ ] Отрисовка только видимых строк (viewport rendering) — [#8](https://github.com/efimov-r/aisdme/issues/8)

Разбор кода, 2026-11-05:

- [ ] Архитектура: состояние в Compose, слои, ответственность контроллеров — [#27](https://github.com/efimov-r/aisdme/issues/27)

Остальное:

- [ ] Реакция на удаление и изменение извне файла, открытого в редакторе — [#9](https://github.com/efimov-r/aisdme/issues/9)
- [ ] Своя панель заголовка окна (custom title bar) — [#10](https://github.com/efimov-r/aisdme/issues/10)
- [ ] Оформление окна выбора проекта — [#21](https://github.com/efimov-r/aisdme/issues/21)
- [ ] Убрать все `copy(alpha = …)` у цветов — [#11](https://github.com/efimov-r/aisdme/issues/11)
- [ ] Подсветка синтаксиса — [#13](https://github.com/efimov-r/aisdme/issues/13)

Тесты и качество (тесты подключены в #14, запуск — `gradlew test`). После новых тестов в #15–17 — поднять порог покрытия Kover (#19), порог только растёт:

- [ ] Тесты логики дерева проекта — [#17](https://github.com/efimov-r/aisdme/issues/17)
