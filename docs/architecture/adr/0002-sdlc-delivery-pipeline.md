# ADR-0002: Development, CI, release and staging delivery pipeline

- **Статус:** Accepted (architecture decision; implementation pending)
- **Дата:** 2026-11-13
- **Область:** PACT Bootstrap v0.1 SDLC, reproducibility, CI, release, staging delivery
- **Связанные Issues:** [#19 DevEx](https://github.com/felikssladkovic-debug/control-tower/issues/19), [#20 Playwright](https://github.com/felikssladkovic-debug/control-tower/issues/20), [#6 CI](https://github.com/felikssladkovic-debug/control-tower/issues/6), [#7 Release](https://github.com/felikssladkovic-debug/control-tower/issues/7), [#8 Deploy](https://github.com/felikssladkovic-debug/control-tower/issues/8), [#9 Acceptance](https://github.com/felikssladkovic-debug/control-tower/issues/9)
- **Контекст:** [ADR-0001](0001-bootstrap-stack.md) определяет монорепозиторий, модульный монолит, Vue, PostgreSQL и Compose. Эта ADR конкретизирует delivery, не меняя продуктовый stack.

## Контекст и проблема

Разработчик работает с Windows/IntelliJ IDEA + SSHFS-Win, основной coding agent — Codex. Код создаётся и тестируется на отдельном headless Ubuntu host по SSH. Другой Linux host уже обслуживает несколько тестовых сайтов через системный Nginx и TLS. Нужны быстрые локальные feedback loops, независимая проверка изменений, реальные браузерные проверки, воспроизводимые Docker images, управляемый deployment и evidence после deployment.

Монорепозиторий хранит также `docs/` и `research/`. Изменение только документов не должно запускать дорогие сборки, публикацию образов или deployment. Изначальный GitHub repo публичный — не хранить там credentials и реальные данные пилотных клиентов.

## Решение

### 1. Роли систем

| Среда | Роль | Допускается | Не допускается |
| --- | --- | --- | --- |
| `ubuntu-dev` | Основная рабочая копия Git, Codex CLI, build/tests, локальный Compose, headless Playwright | Разработка, тестовые Postgres volumes, SSH tunneling | Публикация dev-портов наружу без отдельного решения, работа с staging secrets |
| Windows laptop | IntelliJ IDEA, SSH/SSHFS-Win, browser/manual QA | Просмотр кода/PR, UI через SSH local forwarding | Обязательная вторая синхронизируемая рабочая копия исходников |
| GitHub-hosted Actions | Независимый CI, Release, Deploy orchestration | PR gates, Testcontainers, headless Playwright, GHCR images | Privileged execution непроверенного PR-кода, автоматический deploy из PR |
| `ubuntu-staging` | Общий тестовый runtime host, имя GitHub Environment: `staging` | Docker Engine + Compose, images по digest, существующий Nginx/TLS, persistent DB, health | Maven/npm/создание образов из исходников/Codex, self-hosted CI runner |

Название хоста `ubuntu-staging` заменяет прежнее `ubuntu-prod`; **реальное production окружение в PACT пока не создано**.

### 2. Progressive verification

1. **Local fast (`ubuntu-dev`):** после логически завершённого изменения: формат/линтер/typecheck, Java compile, быстрые unit tests для затронутых частей. Бюджет времени — приоритет скорости, а не полный E2E на каждом коммите.
2. **Local full (перед PR):** все релевантные unit/integration tests, Java `./mvnw -B -ntp verify` с реальным Testcontainers PostgreSQL, Vue `npm ci; npm run lint; npm run typecheck; npm run test; npm run build`, Compose config/smoke и затронутые browser tests. Скрипты `scripts/check-fast.sh` и `scripts/check-full.sh` **планируются** в #19.
3. **Independent PR CI:** те же гарантии, но независимо на GitHub-hosted runner; локальный отчёт Codex — не substitute. CI — единственный обязательный Git merge quality gate.
4. **Browser/System E2E:** Playwright + headless Chromium с настоящими HTTP через frontend reverse proxy, backend и изолированный PostgreSQL; минимум SPA boot, backend online, backend error/offline. Ошибки оставляют screenshots/traces. Реализация — #20.
5. **Release checks:** версионированные образы, проверки безопасности, provenance `tag → commit → test evidence → digest`. Запрещено считать `latest` identity.
6. **Post-deployment verification:** проверка HTTPS, UI/API, health, версии после выкладки и при rollback; успешное `compose up` само по себе не подтверждает deployment.

Тесты должны сообщать `passed/failed/not-run` с командами и evidence; агент не может утверждать успех пропущенной проверки. Если быстрые/интеграционные Java-тесты разнесены в Surefire/Failsafe, ни один существующий интеграционный тест не должен пропасть из полного цикла.

### 3. CI и границы монорепозитория

`.github/workflows/ci.yml` будет запускаться на PR к `main` (и на push в `main` при необходимости release evidence). Первый job определяет изменённые пути, далее условные jobs: backend, frontend, container/integration и Playwright E2E. `docs/**`, `research/**`, обычные root text changes вызывают только недорогой stable `ci-gate`. Изменения build/tooling/workflow/shared interface или ошибка path detection — безопасный полный набор соответствующих проверок (**fail closed**).

**Обязательный `ci-gate` всегда присутствует**: если целый required workflow отключить через `paths-ignore`, GitHub может оставить Pending check. Gate должен отличать ожидаемый skip от неожиданного failure/cancel/skip. Никакие runtime secrets/release credentials не раскрываются в PR CI; `pull_request_target` не запускает недоверенный код.

После успешной верификации workflow требуется защита `main` с обязательным `ci-gate`, по возможности PR review; наличие этой защиты **пока не подтверждено**.

### 4. Release

Отдельный workflow `release.yml` для контролируемого semantic tag `vX.Y.Z` (либо explicit `workflow_dispatch`). Перед публикацией необходимо проверить commit/ref и требуемые tests/CI evidence: опубликованный tag не является доказательством качества. На Bootstrap собираем два образа — `backend`, `frontend` — в GitHub Actions, публикуем в GHCR с version tag, commit SHA и неизменяемыми digest. Записываем идентичность исходников и артефактов. Release **не выполняет SSH/deploy**.

Позднее можно перейти на `build once on main → promote digests` без пересборки на новом окружении. Это возможная эволюция, а не требование Bootstrap. Не вводить сейчас обязательные сложные аттестации, отдельные runners и registry.

### 5. Deployment staging

Отдельный `deploy.yml` по ручному approval/dispatch с параметром уже опубликованных image digests. GitHub Environment — `staging` с доступной на тарифе защитой. Deploy via SSH на `ubuntu-staging` запускает **только** `docker compose pull` / `up -d` immutable digest references; не переносит source или Dockerfile для сборки.

Используется существующий **host Nginx** с имеющимися сертификатами и DNS; PACT frontend container слушает **loopback host port**, backend/DB не публикуются. Внутренний Nginx frontend уже проксирует `/api/**` на backend. Уникальные Compose project name, сети, volumes, порты и лимиты должны не затронуть другие приложения общего staging host. Не придумывать домены/IP.

До deployment: verify digests, SSH host key, permissions и disk space, предыдущую версию, актуальность/доступность резервной копии БД и совместимость Flyway миграций. После: health/status, публичный HTTPS smoke и identity текущего релиза, запись evidence. При ошибке возврат previous digests/Compose config **при условии совместимости схемы**. Откат image не откатывает БД автоматически: предпочтительны backwards-compatible migrations и отдельно проверяемое восстановление.

SSH/registry/deploy credentials ограничены по правам и хранятся в GitHub secrets и защищённых файлах сервера; учесть, что доступ к Docker socket/group практически root-level. Staging не должен превращаться в CI worker.

### 6. Код и документы — source of truth

GitHub Issues → короткие feature branches → PR в `main` → required CI → explicit merge → version tag/release → явный staging deploy → verification. Все значимые действия подтверждаются ссылками на run, commit, image digest и test results. Для исследований и docs-only PR не создаются релизы. `AGENTS.md` и runbooks должны оставаться согласованы с этой ADR.

## Альтернативы и причины выбора

| Альтернатива | Почему не сейчас |
| --- | --- |
| Собирать/тестировать только на `ubuntu-dev` | Агент и тесты разделяют среду; нет независимого gate |
| Собирать исходники непосредственно на `ubuntu-staging` | Неповторяемая сборка, доступ к исходникам/инструментам на общем runtime host, трудно откатывать |
| Перейти на GitLab CI/registry | Есть рабочий GitHub repo; смена провайдера не уменьшает фундаментальную сложность pipeline |
| Self-hosted GitHub runner на staging | Риск privileged execution недоверенного кода рядом с runtime и БД |
| Kubernetes/Argo CD/GitOps уже в Bootstrap | Операционная сложность без подтверждённой потребности |
| Полный E2E при каждом локальном сохранении | Слишком дорогая обратная связь; достаточно быстрых локальных checks и независимого CI |

## Последствия, риски, условия пересмотра

- Два отдельных Docker образа и независимые jobs увеличивают расход CI, но сохраняют простую модель runtime.
- Flaky E2E нельзя «лечить» безлимитными retries; нужен trace/evidence и изоляция тестов.
- GitHub Actions и GHCR требуют контроля разрешений, квот, tag policies, cache и dependency versions.
- Path detection содержит риск пропустить зависимые проверки; любые неочевидные изменения должны проверяться консервативно.
- При изменении backend/frontend contracts, Dockerfiles, compose или shared tooling запускать интеграционные проверки.
- При переходе к реальному production, multi-server HA, CDN, microservices, Kafka, Kubernetes, существенным требованиям комплаенса или потребности в canary/blue-green пересмотреть доставку отдельным ADR.
- GitLab остаётся возможностью миграции, не решением об изменении текущего поставщика.

## Состояние реализации на 2026-11-13

- **Decision:** архитектура delivery pipeline принята данным ADR.
- **Implementation (подтверждено деревом репозитория):** backend/frontend build definitions, unit/integration tests, Dockerfiles, локальный Compose и `scripts/smoke-compose.sh`.
- **Planned (не реализовано):** fast/full scripts, Playwright E2E, `.github/workflows/ci.yml`, release/registry, digest-based staging compose, deploy workflow, staging health/rollback evidence.
- **Verification:** CI, GHCR release и server deployment должны быть подтверждены отдельными run URLs; принятие ADR не означает выполнения #6–#9, #19–#20.
