# Applications

**Статус:** технический backend реализован в `apps/backend/`; frontend ещё не создан.

По [ADR-0001](../docs/architecture/adr/0001-bootstrap-stack.md), будущие приложения:

- [`apps/backend/`](backend/README.md) — один запускаемый Spring Boot backend без бизнес-логики и БД; [Issue #2](https://github.com/felikssladkovic-debug/control-tower/issues/2).
- `apps/frontend/` — Vue 3 + TypeScript SPA; [Issue #3](https://github.com/felikssladkovic-debug/control-tower/issues/3).

Каталоги создаются вместе с реально запускаемыми проектами, тестами, инструкциями запуска и зафиксированными зависимостями. Папка `packages/` появится, только когда возникнет обоснованно разделяемый код.
