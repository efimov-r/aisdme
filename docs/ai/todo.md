# Задачи

Что в работе и что дальше. Выполненное удаляется — история есть в git. Подробности задач — в issues на GitHub.

## В работе

- [ ] Quality gates — [#19](https://github.com/efimov-r/aisdme/issues/19), ветка `issue-19-quality-gates`: detekt, Kover и CI готовы, ждёт push и PR; пользователю — включить защиту `master` (required status checks)

## Проверить на Linux

Код для Linux менялся в [#12](https://github.com/efimov-r/aisdme/issues/12) (PR [#29](https://github.com/efimov-r/aisdme/pull/29)) и проверен только по коду. Проверить на Manjaro (KDE), когда будет возможность:

- [ ] `gradlew run` запускается, подхватывается старый конфиг `~/.config/AisDme/config.json` (последний проект, тема, окно)
- [ ] Дерево обновляется, если в Dolphin создать, переименовать и удалить файл и папку на 2–3 уровня вглубь, в том числе внутри только что созданной извне папки
- [ ] В меню дерева пункт называется «Show in Dolphin»; он и Alt+E на файле и на папке открывают Dolphin с выделенным элементом
- [ ] Вырезать в Dolphin → вставить в дереве: оригинал исчезает. Вырезать в дереве → вставить в Dolphin: файл исчезает из проекта. Копирование в обе стороны по-прежнему копирует
- [ ] Вставка папки в её подпапку: ошибка в диалоге Paste, кнопка OK неактивна

## Дальше

Главное — быстро редактировать большие текстовые файлы, в том числе вставлять в середину, и отменять ввод. Решение — [decisions.md](decisions.md), «Piece Table + LineIndex + Undo». Эпик — [#4](https://github.com/efimov-r/aisdme/issues/4).

- [ ] Piece Table вместо текста в `TextFieldValue` — [#5](https://github.com/efimov-r/aisdme/issues/5)
- [ ] LineIndex — [#6](https://github.com/efimov-r/aisdme/issues/6)
- [ ] Undo / Redo поверх Piece Table — [#7](https://github.com/efimov-r/aisdme/issues/7)
- [ ] Отрисовка только видимых строк (viewport rendering) — [#8](https://github.com/efimov-r/aisdme/issues/8)

Баги:

- [ ] Автоскролл при перетаскивании не останавливается в конце прокрутки — [#3](https://github.com/efimov-r/aisdme/issues/3)
- [ ] Баги из разбора кода: потеря данных, конфиги, потоки, watcher — [#26](https://github.com/efimov-r/aisdme/issues/26). Сначала — потеря данных

Разбор кода, 2026-11-05:

- [ ] Архитектура: состояние в Compose, слои, ответственность контроллеров — [#27](https://github.com/efimov-r/aisdme/issues/27)
- [ ] Производительность: дерево проекта, отслеживание диска, рекомпозиция — [#28](https://github.com/efimov-r/aisdme/issues/28)

Остальное:

- [ ] Реакция на удаление и изменение извне файла, открытого в редакторе — [#9](https://github.com/efimov-r/aisdme/issues/9)
- [ ] Своя панель заголовка окна (custom title bar) — [#10](https://github.com/efimov-r/aisdme/issues/10)
- [ ] Оформление окна выбора проекта — [#21](https://github.com/efimov-r/aisdme/issues/21)
- [ ] Убрать все `copy(alpha = …)` у цветов — [#11](https://github.com/efimov-r/aisdme/issues/11)
- [ ] Подсветка синтаксиса — [#13](https://github.com/efimov-r/aisdme/issues/13)

Тесты и качество (тесты подключены в #14, запуск — `gradlew test`). После новых тестов в #15–17 — поднять порог покрытия Kover (#19), порог только растёт:

- [ ] Тесты конфигов — [#15](https://github.com/efimov-r/aisdme/issues/15)
- [ ] Тесты файловых операций — [#16](https://github.com/efimov-r/aisdme/issues/16)
- [ ] Тесты логики дерева проекта — [#17](https://github.com/efimov-r/aisdme/issues/17)
