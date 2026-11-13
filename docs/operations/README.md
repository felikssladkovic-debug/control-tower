# Operations

**Статус:** локальный integration stack с frontend, backend и PostgreSQL доступен через Compose. Production окружения, релизы и сервер пока не настроены через этот репозиторий.

Будущие документы: CI checks, release/tag/image digests, deployment, rollback и проверенные runbook-команды.

Текущие инструкции: [локальный Compose](local-compose.md) и [PostgreSQL: роли, резервная копия и восстановление](postgresql.md).

Согласованные, пока не проверенные на новых серверах runbooks: [ubuntu-dev + Codex](development.md) и [ubuntu-staging deployment](staging-deployment.md). Роль host `ubuntu-staging` заменила прежнее название `ubuntu-prod`; GitHub Environment будет называться `staging`. Основа — [ADR-0002](../architecture/adr/0002-sdlc-delivery-pipeline.md).

Связанные задачи: [#5 Docker Compose](https://github.com/felikssladkovic-debug/control-tower/issues/5), [#6 CI](https://github.com/felikssladkovic-debug/control-tower/issues/6), [#7 Release](https://github.com/felikssladkovic-debug/control-tower/issues/7), [#8 Deploy](https://github.com/felikssladkovic-debug/control-tower/issues/8), [#9 Acceptance](https://github.com/felikssladkovic-debug/control-tower/issues/9).

Ни один процесс не считается проверенным без реальных evidence: commit, workflow run, release digest и smoke-test output.
