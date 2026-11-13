# Development workflow: ubuntu-dev + Codex (plan / runbook)

> **Статус 2026-11-13:** согласован рабочий процесс; `ubuntu-dev` готовит владелец. Команды ниже опираются на текущий репозиторий, но их исполнение на будущем сервере **не подтверждено**. Изменения в инструментах и скриптах: [#19](https://github.com/felikssladkovic-debug/control-tower/issues/19), Playwright: [#20](https://github.com/felikssladkovic-debug/control-tower/issues/20).

## Роли

- **`ubuntu-dev` (headless):** единственная основная Git working copy, Codex CLI, Java/Maven Wrapper, Node/npm, Docker/Compose, сборка, тесты, Testcontainers и headless Chromium после добавления Playwright.
- **Windows laptop:** IntelliJ IDEA + SSHFS-Win для review/навигации, терминал SSH и обычный browser. Не выполнять конкурентные правки одного и того же working tree с двух клиентов без координации.
- **GitHub:** PR + независимые CI checks. Локальный `passed` не заменяет GitHub gate.
- **`ubuntu-staging`:** только runtime; разработка там не выполняется.

## Host prerequisites (проверить перед использованием)

| Требование | Почему |
| --- | --- |
| Git + SSH key к GitHub с минимумом прав | короткие ветки и PR |
| Java 21, `apps/backend/mvnw` | действительный `pom.xml` использует Java 21 |
| Node/npm в совместимом диапазоне `apps/frontend/package.json` | frontend build/typecheck/Vitest |
| Docker Engine + Compose plugin, доступ Testcontainers к Docker | PostgreSQL integration tests и `smoke-compose.sh` |
| Codex CLI и авторизация, настроенные владельцем | основной coding agent |
| Chromium для Playwright после #20 | headless browser E2E |
| SSH ingress только по утверждённому доступу; firewall | безопасное remote development |

Проверить актуальные версии из файлов, а не фиксировать в этом документе произвольные версии Maven/npm/Docker. Не переносить credentials и реальные customer datasets в public repo.

## Минимальные реальные команды (до появления scripts/check-*.sh)

Из checkout:

```bash
cd apps/frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

```bash
cd apps/backend
./mvnw -B -ntp verify
```

```bash
# из корня репозитория, если установлен Docker:
docker compose config --quiet
./scripts/smoke-compose.sh
```

`./mvnw verify` может запускать Testcontainers; нужен рабочий Docker. `smoke-compose.sh` изолирует временный Compose project и очищает его. Эти команды приводятся как инструкции, не как evidence успешного запуска.

После #19: `scripts/check-fast.sh` для быстрых частых проверок и `scripts/check-full.sh` перед PR; скрипты **пока отсутствуют**. После #20: отдельный `npm run test:e2e` с headless Chromium. Не выдумывать их успешное выполнение до реализации.

## Dev UI через SSH tunnel

Текущий локальный Compose публикует SPA на `127.0.0.1:5606` **только на машине, где запущен Compose**. На Windows:

```bash
ssh -L 5606:127.0.0.1:5606 <ssh-user>@<ubuntu-dev-host>
```

Затем открыть в Windows browser `http://127.0.0.1:5606/`. Запуск `docker compose up --build -d` на `ubuntu-dev` требует отдельных **локальных** паролей в игнорируемом `.env` (см. [local-compose.md](local-compose.md)). Аналогично можно пробрасывать Vite dev-port при необходимости HMR. Не открывать dev UI напрямую в Internet.

## Codex protocol

1. Прочитать `AGENTS.md`, Issue, ADR-0001/0002, фактическое дерево и скрипты; начать короткую ветку от `main`.
2. Для каждого небольшого изменения выполнить соответствующие быстрые проверки; перед PR выполнить применимые full checks. Не обходить провал тестов без записи причины.
3. Для изменений UI проверить реальный browser flow; пока #20 не реализован — явно отметить E2E как `not run`, а не `passed`.
4. Создать PR с изменёнными файлами, связанным Issue, командами, результатами и ограничениями. Не выполнять auto-merge, release/deploy или изменения данных без отдельного разрешения.
5. После merge GitHub CI, release, staging deploy и post-deploy verification фиксируют собственные независимые evidence.

## Развилка по файлам

- Docs/research-only PR: не требовать Java/Node/Docker build (политика будет реализована #6).
- Backend: Maven verify, API/DB integration; frontend: lint/typecheck/test/build; Compose/integration scripts: config/smoke.
- Shared/tooling/неизвестные пути: не делать optimistic skip. Правило CI gate — [ADR-0002](../architecture/adr/0002-sdlc-delivery-pipeline.md).
