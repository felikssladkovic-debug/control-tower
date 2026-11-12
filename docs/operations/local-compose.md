# Локальный Compose: PACT Bootstrap

## Назначение и границы

Это локальный integration stack для Issue #5 и ADR-0001: Vue SPA, Spring Boot backend и PostgreSQL. Это **не** production deployment, не release artifact и не инструкция для публичного сервера; эти решения относятся к #7–#8. В stack нет Kubernetes, Kafka, Redis и других дополнительных сервисов.

`frontend` — единственная host-точка входа. Backend доступен frontend по отдельной Compose application-сети, PostgreSQL — только backend по отдельной `internal` data-сети. Ни backend, ни PostgreSQL не публикуют host-порты.

## Обычный запуск

Из корня репозитория:

```sh
cp .env.example .env
# Заполните три значения собственными локальными паролями; .env не коммитится.
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Откройте <http://127.0.0.1:5606>. UI показывает online и версию после успешного `GET /api/system/info`, который Nginx проксирует в backend. Проверить API через тот же origin можно так:

```sh
curl --fail http://127.0.0.1:5606/api/system/info
```

`PACT_UI_PORT` в `.env` задаёт loopback-порт UI и по умолчанию равен `5606`; изменяйте его только при локальном конфликте порта.

Все три сервиса должны показать `healthy` в `docker compose ps`. PostgreSQL healthcheck проверяет готовность БД; backend проверяет Actuator readiness с подключением к БД; frontend проверяет выдачу SPA. Обновление или прямое открытие client-side URL, например <http://127.0.0.1:5606/unknown/path>, должно отдать SPA, а не Nginx 404.

## Логи, остановка и данные

```sh
docker compose logs --follow frontend
docker compose logs --follow backend
docker compose logs --follow postgres
docker compose down
```

`docker compose down` удаляет контейнеры и сети, но сохраняет named volume PostgreSQL. Последующий `docker compose up --build -d` использует те же локальные данные.

Полный сброс **необратимо** удаляет только локальный Compose volume:

```sh
docker compose down --volumes
```

Не передавайте `.env`, вывод с паролями или дампы БД в Git, issue и логи.

## Разработка с Vite

Обычный Compose не открывает backend на host. Для Vite включите явный локальный override:

```sh
docker compose -f compose.yaml -f compose.dev.yaml up --build -d
cd apps/frontend
npm run dev
```

`compose.dev.yaml` добавляет только `127.0.0.1:8086:8080`; Vite proxy направляет `/api` на этот loopback-порт. Порт не доступен из сети и отсутствует при обычном запуске. Остановить dev stack можно той же парой файлов:

```sh
docker compose -f compose.yaml -f compose.dev.yaml down
```

## Smoke-проверка

`scripts/smoke-compose.sh` требует Docker, grep, mktemp, od и tr. Он создаёт отдельный Compose project и временный volume, генерирует пароли в памяти, проверяет published port mapping, UI/API/SPA fallback через Nginx и persistence marker-данных после обычного `down`. Его cleanup удаляет только временные ресурсы тестового project:

```sh
./scripts/smoke-compose.sh
```

По умолчанию smoke использует `127.0.0.1:15606`, чтобы не конфликтовать с обычным локальным UI. При необходимости укажите свободный порт через `PACT_SMOKE_UI_PORT`; скрипт не заменяет production verification.
