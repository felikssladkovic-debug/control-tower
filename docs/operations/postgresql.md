# PostgreSQL для Bootstrap v0.1

## Версия и роли

Локальный Compose и Testcontainers используют PostgreSQL `17.11-trixie`. Это зафиксированный тег образа, а не автоматическое обновление minor-версии. Обновление выполняется отдельным проверяемым изменением. Spring Boot, драйвер PostgreSQL и Flyway версии берутся из Maven dependency management Spring Boot 4.1.2.

Есть три локальных учётных записи: `postgres` только для первичной настройки и операций администратора, `pact_migrator` для Flyway, `pact_app` для runtime. Backend не получает пароль `postgres`. В production создаётся отдельная БД с теми же именами ролей и другими паролями, поступающими из секретов окружения. Роль мигратора требует `CONNECT, CREATE` на БД, чтобы Flyway создал схему `pact`; роль приложения требует `CONNECT` на БД, а после `V1` — `USAGE` на схему. Доступ к таблицам выдаётся будущими предметными миграциями по необходимости.

При подготовке production БД администратор создаёт БД `pact`, роли и выдаёт права, соответствующие [локальному init-скрипту](../../infra/postgres/init/01-roles.sh). Пароли передаются администратору через одобренный секретный канал; SQL-файлы с реальными значениями не сохраняются в репозитории. Миграция выполняется при запуске backend под `pact_migrator`. Никаких действий с production эта инструкция сама не выполняет.

## Локальный запуск и диагностика

1. Из корня репозитория скопировать `.env.example` в `.env`, заполнить все три пароля и запустить `docker compose config --quiet`.
2. Выполнить `docker compose up --build -d` и проверить `docker compose ps`, `docker compose logs backend`, `http://127.0.0.1:8080/actuator/health/readiness`.
3. Для остановки без удаления данных выполнить `docker compose down`. Повторный `up` использует прежний volume и не применяет `V1` заново.

PostgreSQL не публикует порт хоста. Доступ администратора локально возможен через `docker compose exec postgres psql -U postgres -d pact`. Не публикуйте содержимое `.env` и логи с секретами. Смена переменной пароля в `.env` не меняет пароль уже созданной роли: init-скрипт выполняется только при пустом volume. Для изменения существующей роли выполните управляемый `ALTER ROLE` в PostgreSQL и синхронно обновите секрет приложения. Удаление локальных тестовых данных: `docker compose down -v`; эта команда необратимо удаляет локальный volume PostgreSQL.

## Резервная копия и восстановление

Следующие команды — инструкция для локальной БД; автоматический тест восстановления в #4 не реализован. Сохраняйте файл дампа вне публичного репозитория: он может содержать конфиденциальные данные.

```sh
umask 077
docker compose exec -T postgres pg_dump -U postgres -d pact -Fc > /tmp/pact-local.dump
```

Для восстановления используйте **отдельную пустую БД/volume** и заранее создайте роли с теми же именами. Восстановление в работающую production БД требует отдельного плана остановки, проверки резервной копии и отката. Локально отдельное имя Compose-проекта создаёт новый volume; запускайте сначала только PostgreSQL, чтобы backend не применил `V1` до восстановления:

```sh
docker compose -p pact-restore up -d postgres
docker compose -p pact-restore exec -T postgres pg_restore -U postgres --role=pact_migrator --no-owner -d pact < /tmp/pact-local.dump
docker compose -p pact-restore exec -T postgres psql -U postgres -d pact -Atqc "SELECT version FROM pact.flyway_schema_history WHERE version IS NOT NULL"
```

Затем проверьте наличие строки `V1` в `pact.flyway_schema_history`, права роли `pact_app`, запуск backend и readiness в изолированном окружении. `pg_dump` сохраняет схему и данные конкретной БД, но не глобальные роли и их пароли; их создают отдельно до восстановления. Секреты и дамп не коммитят в Git. Остановите проверочный проект командой `docker compose -p pact-restore down`; volume останется для анализа.
