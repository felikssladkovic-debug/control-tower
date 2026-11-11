# Infrastructure

**Статус:** есть локальный init-скрипт PostgreSQL; production конфигурация и автоматическое развёртывание ещё не реализованы.

По мере реализации сюда будут помещены `infra/deploy/` (Docker Compose production и инструкции), `infra/provisioning/` (только если необходима автоматизация подготовки сервера).

[`infra/postgres/init/01-roles.sh`](postgres/init/01-roles.sh) создаёт только локальные роли при первой инициализации Compose volume. Production роли создаются отдельно по [операционной инструкции](../docs/operations/postgresql.md).

План: Docker/Compose, GitHub Actions, GHCR, контролируемый production deployment, health verification и rollback. Это запланировано в [#5](https://github.com/felikssladkovic-debug/control-tower/issues/5), [#7](https://github.com/felikssladkovic-debug/control-tower/issues/7), [#8](https://github.com/felikssladkovic-debug/control-tower/issues/8).

Секреты, серверные private keys, рабочие `.env` и реальные данные не помещать в этот публичный репозиторий.
