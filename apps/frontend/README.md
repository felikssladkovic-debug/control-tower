# PACT frontend

Техническая Vue SPA для Bootstrap v0.1 ([Issue #3](https://github.com/felikssladkovic-debug/control-tower/issues/3), [ADR-0001](../../docs/architecture/adr/0001-bootstrap-stack.md)). Реализованы Overview, Not Found, двуязычный shell и отображение версии доступного backend API. Предметных данных, аутентификации и бизнес-показателей нет.

## Требования и запуск

Нужен Node.js 22.22.2+ (или 24.15+) и npm. В `apps/frontend/`:

```sh
npm ci
npm run dev
```

Для интегрированного запуска из корня репозитория заполните `.env` по `.env.example` и выполните `docker compose up --build -d`. UI доступен только по <http://127.0.0.1:5606>; Nginx раздаёт собранную SPA, проксирует `/api/**` во внутренний backend и возвращает `index.html` для client-side маршрутов при refresh.

`npm run dev` запускает Vite отдельно. Чтобы Vite мог обратиться к backend, сначала явно включите локальный debug port: `docker compose -f compose.yaml -f compose.dev.yaml up --build -d`. В этом режиме proxy направляет `/api` на `http://127.0.0.1:8086`; этот порт привязан только к loopback и отсутствует в обычном Compose. Если backend не запущен, frontend остаётся доступным и показывает отсутствие соединения.

Статус проверяется при открытии, каждые 30 секунд и кнопкой повтора. `API доступен` означает только корректный ответ `GET /api/system/info`, а не готовность базы данных или всей системы. Сетевой сбой отображается как отсутствие соединения; HTTP-ошибка или некорректный JSON — как ошибка получения сведений. Версия показывается только после актуального успешного ответа. Русский язык выбран по умолчанию; выбор русского или английского сохраняется в браузере.

## Проверки и сборка

```sh
npm run typecheck
npm run lint
npm run test
npm run build
npm run preview
```

`npm run lint` включает ESLint и проверку форматирования Prettier. `npm run preview` служит только для локальной проверки собранного приложения, не для production. Compose-образ использует Nginx с SPA fallback и отдельным проксированием `/api/**`; команды эксплуатации находятся в [локальной Compose-инструкции](../../docs/operations/local-compose.md).
