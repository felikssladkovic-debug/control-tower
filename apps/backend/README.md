# PACT backend

Технический Spring Boot backend для Bootstrap v0.1. Один Maven-проект и один запускаемый JAR. PostgreSQL подключён через JDBC и Flyway; предметной модели и бизнес-данных нет. Полный Compose с frontend относится к [#5](https://github.com/felikssladkovic-debug/control-tower/issues/5).

## Запуск и проверка через Docker

Из корня репозитория:

```sh
cp .env.example .env
# Заполните три обязательных значения в .env собственными локальными паролями.
docker compose config --quiet
docker compose up --build -d
docker compose ps
docker compose logs backend
docker compose down
```

Compose запускает PostgreSQL 17.11 (образ `postgres:17.11-trixie`) с постоянным томом и backend только после healthcheck БД. Для `docker compose up` на хосте достаточно Docker; Java и Maven не нужны. Docker build собирает JAR с `-DskipTests`: успешная сборка образа **не подтверждает** интеграционные тесты. Для полной проверки нужен Java 21, Docker daemon и команда `cd apps/backend && ./mvnw verify`; она запускает PostgreSQL через Testcontainers. После изменений исходников повторите `docker compose up --build -d`.

Compose запускает backend с профилем `local` и публикует порт **только на `127.0.0.1:8080`**. Проверить ответы можно браузером или уже установленным HTTP-клиентом:

| Адрес | Назначение |
| --- | --- |
| <http://127.0.0.1:8080/api/system/info> | Имя `PACT` и версия Maven-проекта, начиная с `0.1.0-SNAPSHOT` |
| <http://127.0.0.1:8080/actuator/health/liveness> | Состояние процесса |
| <http://127.0.0.1:8080/actuator/health/readiness> | Готовность принимать запросы |
| <http://127.0.0.1:8080/v3/api-docs> | OpenAPI JSON в `local`/`test` |
| <http://127.0.0.1:8080/swagger-ui.html> | Swagger UI в `local`/`test` |

`/api/system/info` возвращает только `name` и `version`. Ответ HTTP 200 показывает, что этот API доступен; состояние здоровья следует проверять через Actuator. Readiness выполняет проверку соединения с PostgreSQL под ролью приложения; при потере БД возвращает HTTP 503. Liveness не зависит от БД. Если БД недоступна при старте, приложение не запускается. Наружу опубликован только Actuator `health`, без `env`, `configprops` и других диагностических endpoint. OpenAPI JSON и Swagger UI отключены в профиле `prod`.

Неизвестный путь возвращает стандартный HTTP 404; непредвиденная ошибка сервера — HTTP 500. Собственный формат ошибок в #2 не вводится.

## Конфигурация и границы кода

`SPRING_PROFILES_ACTIVE` выбирает профиль `local`, `test` или `prod`; профили задают доступность документации API. `PACT_DB_JDBC_URL` задаёт JDBC URL; `PACT_DB_APP_PASSWORD` и `PACT_DB_MIGRATOR_PASSWORD` задают разные пароли ролей `pact_app` и `pact_migrator`. `PACT_DB_ADMIN_PASSWORD` требуется только локальному контейнеру при инициализации БД и не передаётся backend. Пул Hikari по умолчанию ограничен пятью соединениями и трёхсекундным таймаутом; `PACT_DB_POOL_MAX_SIZE` и `PACT_DB_CONNECTION_TIMEOUT_MS` позволяют изменить эти значения. В prod БД и роли создаются заранее управляемой операционной процедурой; credentials поступают из секретов окружения, а не из Git. Dev и prod используют отдельные БД и разные пароли.

Flyway работает под `pact_migrator`, создаёт схему `pact` и хранит историю миграций в ней. `V1` выдаёт `pact_app` только `USAGE` на схему; ни доменных, ни постоянных тестовых таблиц не создаёт. При повторном старте Flyway проверяет историю и не выполняет `V1` снова. Создание ролей и выдача `CREATE` на БД выполняются до запуска Flyway. При уже существующем Compose volume init-скрипт повторно не выполняется: изменение `.env` не меняет пароли в PostgreSQL, для этого нужна отдельная команда `ALTER ROLE` или создание нового локального volume.

Для локального backup/restore, включая ограничения и порядок подготовки ролей, см. [операционную инструкцию](../../docs/operations/postgresql.md). Восстановление не запускается автоматически и должно проверяться отдельно перед production применением.

Базовый пакет — `io.feliks.pact`. В `bootstrap` находится точка входа приложения, в `system` — технический HTTP-контракт. Новые пакеты добавляются под конкретные функции, с тестами и явными границами; заранее создавать предметные сущности или отдельные deployable-модули не нужно. Стек и ограничения определены [ADR-0001](../../docs/architecture/adr/0001-bootstrap-stack.md).
