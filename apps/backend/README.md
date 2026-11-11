# PACT backend

Технический Spring Boot backend для Bootstrap v0.1. Один Maven-проект и один запускаемый JAR. Предметной модели, PostgreSQL и бизнес-данных пока нет; подключение БД относится к [#4](https://github.com/felikssladkovic-debug/control-tower/issues/4), полный Compose — к [#5](https://github.com/felikssladkovic-debug/control-tower/issues/5).

## Запуск и проверка через Docker

Из корня репозитория:

```sh
docker compose up --build -d
docker compose ps
docker compose logs backend
docker compose down
```

При `docker compose build` в builder-контейнере запускается `./mvnw verify`. Если тесты не прошли, runtime-образ не будет создан. Java, Maven и PostgreSQL на хосте не нужны. После изменений исходников повторите `docker compose up --build -d`; автоматического перезапуска при редактировании нет.

Compose запускает backend с профилем `local` и публикует порт **только на `127.0.0.1:8080`**. Проверить ответы можно браузером или уже установленным HTTP-клиентом:

| Адрес | Назначение |
| --- | --- |
| <http://127.0.0.1:8080/api/system/info> | Имя `PACT` и версия Maven-проекта, начиная с `0.1.0-SNAPSHOT` |
| <http://127.0.0.1:8080/actuator/health/liveness> | Состояние процесса |
| <http://127.0.0.1:8080/actuator/health/readiness> | Готовность принимать запросы |
| <http://127.0.0.1:8080/v3/api-docs> | OpenAPI JSON в `local`/`test` |
| <http://127.0.0.1:8080/swagger-ui.html> | Swagger UI в `local`/`test` |

`/api/system/info` возвращает только `name` и `version`. Ответ HTTP 200 показывает, что этот API доступен; состояние здоровья следует проверять через Actuator. До реализации #4 readiness не учитывает PostgreSQL. Наружу опубликован только Actuator `health`, без `env`, `configprops` и других диагностических endpoint. OpenAPI JSON и Swagger UI отключены в профиле `prod`.

Неизвестный путь возвращает стандартный HTTP 404; непредвиденная ошибка сервера — HTTP 500. Собственный формат ошибок в #2 не вводится.

## Конфигурация и границы кода

`SPRING_PROFILES_ACTIVE` выбирает профиль `local`, `test` или `prod`; профили задают доступность документации API. Настройки Spring Boot можно переопределять переменными окружения. Секретов и подключений к внешним системам в #2 нет; реальные значения `.env` не следует помещать в репозиторий.

Базовый пакет — `io.feliks.pact`. В `bootstrap` находится точка входа приложения, в `system` — технический HTTP-контракт. Новые пакеты добавляются под конкретные функции, с тестами и явными границами; заранее создавать предметные сущности или отдельные deployable-модули не нужно. Стек и ограничения определены [ADR-0001](../../docs/architecture/adr/0001-bootstrap-stack.md).
