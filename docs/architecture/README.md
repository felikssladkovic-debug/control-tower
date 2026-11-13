# Architecture

Отправная точка: минимальный модульный монолит и отдельное SPA. Мы не делаем универсальную инфраструктуру раньше подтверждённых требований домена.

## Architecture Decision Records

- [ADR-0001 — Bootstrap technology baseline](adr/0001-bootstrap-stack.md) — согласованный стартовый стек.
- [ADR-0002 — Development, CI, release and staging delivery pipeline](adr/0002-sdlc-delivery-pipeline.md) — принятая архитектура SDLC; CI, release и staging deployment пока не реализованы.
- [ADR template](adr/template.md) — контекст, варианты, аргументы, последствия, условия пересмотра.

Новый архитектурный выбор оформляйте через ADR, с ссылками на исследование/Issue, если он существенно меняет технологические или концептуальные границы.

Статусы: `Proposed`, `Accepted`, `Superseded`, `Rejected`. `Accepted` означает принятое решение, **не** доказанную реализацию.

## Не фиксировано

Универсальная модель объектов, онтология, event sourcing, workflow engine, CQRS, графовая БД, мультитенантность, microservices, Kubernetes и AI execution не являются решениями Bootstrap.
