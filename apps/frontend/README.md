# PACT frontend

Техническая Vue SPA для Bootstrap v0.1 ([Issue #3](https://github.com/felikssladkovic-debug/control-tower/issues/3), [ADR-0001](../../docs/architecture/adr/0001-bootstrap-stack.md)). Реализованы Overview, Not Found, двуязычный shell и отображение версии доступного backend API. Предметных данных, аутентификации и бизнес-показателей нет.

## Требования и запуск

Нужен Node.js 22.22.2+ (или 24.15+) и npm. В `apps/frontend/`:

```sh
npm ci
npm run dev
```

Vite выводит локальный URL в терминале. Dev server проксирует `/api` на `http://127.0.0.1:8080`. Backend можно запустить из корня репозитория командой `docker compose up --build -d`. Если backend не запущен, frontend остаётся доступным и показывает отсутствие соединения.

Статус проверяется при открытии, каждые 30 секунд и кнопкой повтора. `API доступен` означает только корректный ответ `GET /api/system/info`, а не готовность базы данных или всей системы. Сетевой сбой отображается как отсутствие соединения; HTTP-ошибка или некорректный JSON — как ошибка получения сведений. Версия показывается только после актуального успешного ответа. Русский язык выбран по умолчанию; выбор русского или английского сохраняется в браузере.

## Проверки и сборка

```sh
npm run typecheck
npm run lint
npm run test
npm run build
npm run preview
```

`npm run lint` включает ESLint и проверку форматирования Prettier. `npm run preview` служит только для локальной проверки собранного приложения, не для production. Режим Vue Router с обычными URL требует от production web server отдавать `index.html` для неизвестного клиентского маршрута, сохраняя отдельное проксирование `/api/**`. Dockerfile, статический сервер, reverse proxy и сквозная проверка refresh относятся к [Issue #5](https://github.com/felikssladkovic-debug/control-tower/issues/5).
