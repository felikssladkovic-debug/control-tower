# ADR-0001: Bootstrap technology baseline

- **Статус:** Accepted (for Bootstrap v0.1 only)
- **Дата:** 2026-11-10
- **Связанные задачи:** [#1](https://github.com/felikssladkovic-debug/control-tower/issues/1), [#2](https://github.com/felikssladkovic-debug/control-tower/issues/2), [#3](https://github.com/felikssladkovic-debug/control-tower/issues/3), [#4](https://github.com/felikssladkovic-debug/control-tower/issues/4), [#5](https://github.com/felikssladkovic-debug/control-tower/issues/5), [#6](https://github.com/felikssladkovic-debug/control-tower/issues/6), [#7](https://github.com/felikssladkovic-debug/control-tower/issues/7), [#8](https://github.com/felikssladkovic-debug/control-tower/issues/8)

## Контекст

PACT исследует модель реального мира и контур управления бизнес-процессами. Первый пилот планируется для кофеен в Гане; гипотезы универсальности требуется проверять также на рыбных фермах УЗВ. До возникновения проверенных потребностей нужна **запускаемая техническая болванка без бизнес-функционала**.

Приоритет — быстрая обратная связь, понятность кода, воспроизводимое развертывание на одном сервере и изоляция исследовательских документов от CI/release/deploy.

## Решение

1. **Монорепозиторий**: `research/` — исследования; `docs/` — Vision, продукт и ADR; `apps/` — production приложения; `labs/` — исполняемые прототипы; `infra/` — развёртывание; `.github/workflows/` — CI/CD. Директории создаются по мере реального содержимого.
2. **Frontend:** Vue 3, TypeScript (strict), Vite, Tailwind CSS 4, PrimeVue 4, Vue Router. Foundry/Blueprint — ориентир для плотного desktop UX, **не React-зависимость и не цель пиксельного копирования**. Лёгкий shell; дизайн-токены; no fake business data.
3. **Backend:** Java 21 baseline, Spring Boot, Maven + Maven Wrapper, REST API, OpenAPI 3 + Swagger UI в dev/test, Spring Boot Actuator. **Один deployable модульный монолит**, границы по бизнес-возможностям/пакетам, не микросервисы. Точная совместимая версия Spring Boot + springdoc выбирается и фиксируется с зависимостями в задаче #2.
4. **Storage:** PostgreSQL как primary/master storage; миграции Flyway, Testcontainers для интеграционных тестов. Структура предметной БД **не определяется** этой ADR.
5. **Local runtime:** Docker multi-stage images и Docker Compose для frontend, backend и PostgreSQL; один внешний origin, внутренние сервисы закрыты. Пока без Kubernetes.
6. **CI:** GitHub Actions с фильтрацией по изменённым путям; docs/research-only изменения не должны запускать сборку frontend/backend или публикацию/развёртывание. Для PR нужен стабильный required gate, не зависающий на пропущенном workflow. Реализация в #6.
7. **Release & deploy:** отдельные управляемые workflows; версионированные immutable Docker images (план — GHCR) и Docker Compose на подготовленном Linux-сервере, с ручным контролем production, проверкой health и откатом. Конкретная инфраструктура ещё не настроена.

## Почему

- **Vue + PrimeVue:** соответствует выбранному Vue stack и обеспечивает богатые компоненты под enterprise UX. Альтернатива React + Blueprint упростила бы прямое использование Blueprint, но противоречит выбранному frontend stack.
- **Spring Boot modular monolith:** одна развёртываемая единица, простая отладка и транзакционные границы на старте. Альтернатива — микросервисы — преждевременно увеличивает операционную сложность.
- **PostgreSQL:** знакомое, надёжное реляционное хранилище; графовые или event-store механизмы должны обосновываться задачами, а не видением.
- **Compose:** достаточен для первичного сервера и экспериментов. Kubernetes добавит сложность до подтверждения потребности.
- **GitHub monorepo / path-aware CI:** исследования и код связаны историей, но не вынуждают пересобирать приложение при каждом изменении Markdown.

## Компромиссы и последствия

- Frontend и backend раздельно собираются и тестируются, но хранятся вместе.
- Отдельные логические backend-модули не равны независимым сервисам; границы придётся эволюционно уточнять.
- PostgreSQL не гарантирует «универсальную модель»: это решение о хранилище, а не об онтологии.
- Docker Compose ограничивает масштабируемость/отказоустойчивость; при росте требований может потребоваться другой способ эксплуатации.
- PrimeVue не гарантирует визуального и функционального соответствия Foundry.
- Сборки, CI, deployment и end-to-end проверки **ещё не реализованы**. Accepted ADR не равна выполненной задаче.

## Явные не-цели Bootstrap

Кофейни CRUD, универсальная онтология, графовая модель, междоменный orchestration, multi-tenancy, SSO/RBAC, CQRS/Event Sourcing, Kafka, workflow engine, DSL, Kubernetes, AI-agent autonomy и production-grade HA.

## Условия пересмотра

Создать новый ADR или явно ревизовать этот при: доказанной необходимости других транзакционных границ или масштаба; несовместимости фреймворков; существенных UX-ограничениях PrimeVue; требованиях к HA/безопасности/изоляции данных, неподдерживаемых Compose; подтверждённой потребности в графовых/event-store механизмах; изменении продуктового пилота.

## Evidence и implementation

Основание решения — согласованный технологический выбор в Issue #1 и связанных постановках. Это **Decision**, но не результат экспериментального сравнения. Измерения производительности, удобства, стоимости внедрения и бизнес-пользы будут проводиться отдельно.

- **Implementation:** технический backend #2, frontend shell #3 и PostgreSQL/Flyway #4 реализованы; полный Compose с frontend, CI, release и deploy пока не реализованы.
- **Verification:** локальные проверки backend указываются в PR #2; сквозная проверка Bootstrap остаётся в #9.
