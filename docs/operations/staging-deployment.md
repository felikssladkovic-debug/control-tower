# Staging deployment and verification (design / preparation checklist)

> **Статус 2026-11-13: Planned, not implemented and not verified.** Этот документ описывает целевой runbook согласно [ADR-0002](../architecture/adr/0002-sdlc-delivery-pipeline.md). Создание pipeline/Compose — [#8](https://github.com/felikssladkovic-debug/control-tower/issues/8), acceptance — [#9](https://github.com/felikssladkovic-debug/control-tower/issues/9).

## Имена и границы

- **Host:** `ubuntu-staging` (ранее назывался `ubuntu-prod`; переименование реального host/DNS выполняет владелец и ещё не подтверждено).
- **GitHub Environment:** `staging`, не `production`. Использовать доступные protection/approval rules; проверить настройки и права для плана GitHub.
- **Application:** PACT, собственный Compose project `pact-staging` (имя — рекомендация, проверить, что не занято), отдельные volumes/networks и host port.
- **Public entry:** существующий host Nginx с HTTPS/certificates/domain, не контейнер PACT напрямую. Без публикации backend и PostgreSQL.
- **Runtime artifacts:** immutable GHCR `frontend@sha256:<digest>` и `backend@sha256:<digest>`, построенные в #7.

Реальные domain, IP, SSH user, port и пути на сервере намеренно не записаны: выбрать после инвентаризации существующей Nginx/Compose инфраструктуры. Не изменять и не перезапускать чужие сайты без плана и согласования.

## До provisioning

1. Проверить ресурсы shared host (disk, memory, открытые порты, существующие Compose projects, volumes, Nginx vhosts, backup policy).
2. Выделить отдельные filesystem paths/config/env для PACT; чувствительные значения вне public repo с корректными Unix permissions. Docker group == практически privileged access; ограничить техническую identity и доступ.
3. Подготовить SSH identity и pinned host key для deploy workflow; secrets GitHub Environment и staging runtime secrets, необходимые только для чтения image из GHCR и работы сервиса. Не сохранять SSH private keys в git.
4. Определить на host **loopback-only** port для frontend; Nginx vhost `HTTPS → http://127.0.0.1:<port>`. Действующий frontend nginx продолжает проксировать `/api` на внутреннюю backend service.
5. Проверить isolated PostgreSQL persistent volume, backup+restore procedures и rollback limitations Flyway. Не использовать destructive `docker compose down --volumes` на staging.

## Promotion / deploy sequence (будущая автоматизация)

1. Выбрать approved release и **конкретные digest обоих образов**; проверить tag/commit/CI evidence и доступность digests.
2. До изменения сервера записать current/previous digests и конфигурацию, проверить backup DB и migrations compatibility, место на диске и SSH.
3. Получить approval при включённых environment protection, безопасно доставить только версию/Compose config, выполнить `docker compose pull` и `docker compose up -d` с `--no-build` по смыслу политики (на deployment host отсутствуют `build:` paths).
4. Дождаться health containers; выполнить smoke по HTTPS (SPA, `/api/system/info`, ожидаемая version), внутренний backend readiness и DB connectivity по защищённому пути.
5. Записать run URL, release, commit, image digests, timestamps, health/smoke output и найденные проблемы. Только после успеха объявить Deploy verified.

**Это последовательность действий, а не выполненные команды.** Указываемые в будущем shell scripts и `infra/deploy/compose.staging.yaml` пока отсутствуют.

## Failure / rollback

- Если проблема в инфраструктуре/образе и DB schema совместима: вернуть сохранённые previous digests и Compose configuration, повторить health/HTTPS smoke.
- Если применённая Flyway migration несовместима с предыдущим backend: rollback образа может быть **опасен**. Остановить автоматический rollback и применить согласованный recovery plan, использовать verified backup/restore при необходимости.
- Всегда записывать фактический результат и версию после аварии; не скрывать partially successful migration.

## Evidence checklist

- [ ] Staging host и выбранный локальный port инвентаризированы.
- [ ] Nginx/TLS config подтверждён без нарушения других проектов.
- [ ] SSH + registry access ограничены, secrets не в public repo.
- [ ] PostgreSQL backup и recovery проверены.
- [ ] Release images доступны по pinned digest.
- [ ] Реальный Deploy run от GitHub Environment `staging` прошёл.
- [ ] HTTPS frontend/API, readiness и release identity проверены.
- [ ] Rollback rehearsal и ограничения записаны.

Существующий `compose.yaml` — **локальный integration stack с `build:`**, не staging manifest. Не выполнять его как production/staging deployment без изменения #8.
