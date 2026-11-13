# PACT — Pan-African Commerce Control Tower

Исследование и разработка платформы для моделирования, наблюдения и управления реальными бизнес-процессами.

> **Статус:** Bootstrap v0.1 в работе. Реализованы технический Spring Boot backend без предметной логики, подключение PostgreSQL/Flyway, Vue frontend shell без бизнес-данных и локальный Compose integration stack. CI/CD, релизы и deployment **не реализованы**. Зеленая сборка не доказывает экономическую пользу продукта.

## Зачем мы создаём PACT

Стратегическая гипотеза PACT: внутри системы поддерживается проверяемая цифровая модель реального мира — объектов, свойств, связей, состояний, событий, процессов и допустимых действий. UI, аналитика и AI взаимодействуют с ней как разные интерфейсы. Модель **не равна** реальности: факты, наблюдения, вычисления, предположения, прогнозы и команды должны различаться.

Ориентир на полный цикл управления: **Observe → Understand → Decide → Act → Verify**.

**Первый пилот:** кофейни в Гане. Цель — измеримая польза от интеграции с существующими POS, процессами и оборудованием; соглашение с пилотным партнёром и ценность пока не подтверждены. Универсальность исследуется также на контрастном домене **рыбных ферм УЗВ**. Масштабирование до иных отраслей — видение, а не принятое требование.

Сначала изучаем конкретные проблемы и ограничения домена, затем выбираем архитектуру. Не создаём «универсальную онтологию» до проверки её необходимости.

## Структура и правила

| Путь | Назначение | Текущее состояние |
| --- | --- | --- |
| [research/](research/README.md) | Источники, сравнения, гипотезы, эксперименты | Подготовлены правила и шаблоны |
| [docs/vision/](docs/vision/README.md) | Стратегическое видение и границы | Подготовлен индекс |
| [docs/product/](docs/product/README.md) | Проблемы, сценарии, метрики пользы | Подготовлен индекс |
| [docs/architecture/](docs/architecture/README.md) | Архитектура, ADR, компромиссы | [ADR-0001](docs/architecture/adr/0001-bootstrap-stack.md) (стек) и [ADR-0002](docs/architecture/adr/0002-sdlc-delivery-pipeline.md) (SDLC delivery; реализация ожидается) |
| [apps/](apps/README.md) | Production-приложения | Есть технический backend и frontend shell |
| [labs/](labs/README.md) | Исполняемые технические эксперименты | **Планируется**, кода нет |
| [infra/](infra/README.md) | Конфигурация инфраструктуры и deploy | **Планируется**, конфигурации нет |
| [docs/operations/](docs/operations/README.md) | Инструкции эксплуатации и evidence | Локальный Compose, [ubuntu-dev](docs/operations/development.md), [staging deployment](docs/operations/staging-deployment.md) (последний пока план) |
| [AGENTS.md](AGENTS.md) | Правила для Codex и других coding-агентов | Созданы |
| [.github/pull_request_template.md](.github/pull_request_template.md) | Контрольный список PR | Создан |

Папки `packages/` и `.github/workflows/` создаются, когда в них появятся реальные библиотеки или workflow. Отсутствие пустого каталога не означает отказ от него.

**Теги зрелости результатов:** `Vision` — стратегическая возможность; `Hypothesis` — проверяемое предположение; `Decision` — принятое и обоснованное решение; `Implementation` — реально сделанное и проверенное. Отдельно фиксируем степень достоверности данных.

## Запуск

Локальная первая версия PACT (Vue frontend, Spring Boot backend и PostgreSQL) запускается из корня через `docker compose up --build -d` после заполнения `.env` по [.env.example](.env.example). Единственная host-точка входа — <http://127.0.0.1:5606>; `/api/**` проходит через frontend reverse proxy. Backend и PostgreSQL не публикуют host-порты. Подробные команды, диагностика, dev override и правила сброса данных приведены в [локальной Compose-инструкции](docs/operations/local-compose.md).

`compose.yaml` — локальный integration stack, а не production deployment; production release/deployment остаются задачами #7–#8. Сборка backend-образа пропускает тесты; `./mvnw verify` с Testcontainers следует запускать отдельно на машине с Java 21 и Docker.

Очередь Bootstrap v0.1:

1. [#1 — Структура, документы, AGENTS.md](https://github.com/felikssladkovic-debug/control-tower/issues/1)
2. [#2 — Spring Boot backend](https://github.com/felikssladkovic-debug/control-tower/issues/2) и [#3 — Vue workspace](https://github.com/felikssladkovic-debug/control-tower/issues/3)
3. [#4 — PostgreSQL / Flyway](https://github.com/felikssladkovic-debug/control-tower/issues/4), [#5 — Docker Compose](https://github.com/felikssladkovic-debug/control-tower/issues/5)
4. [#19 — ubuntu-dev / Codex](https://github.com/felikssladkovic-debug/control-tower/issues/19), [#20 — Playwright](https://github.com/felikssladkovic-debug/control-tower/issues/20), [#6 — CI](https://github.com/felikssladkovic-debug/control-tower/issues/6), [#7 — Release](https://github.com/felikssladkovic-debug/control-tower/issues/7), [#8 — ubuntu-staging Deploy](https://github.com/felikssladkovic-debug/control-tower/issues/8), [#9 — Verification](https://github.com/felikssladkovic-debug/control-tower/issues/9)

## Как участвовать

1. Проверьте соответствующий [Issue](https://github.com/felikssladkovic-debug/control-tower/issues) и существующие ADR.
2. Работайте в короткой ветке: `research/...`, `docs/...`, `feat/...`, `fix/...`, `infra/...`.
3. Откройте Pull Request в `main` по [шаблону](.github/pull_request_template.md); свяжите с Issue, укажите реальный статус тестов и evidence. Не вносите непроверенные архитектурные допущения.
4. Изменение только `research/` и `docs/` **не должно** запускать тяжёлую сборку, release и deploy. Реализация path-aware CI запланирована в [#6](https://github.com/felikssladkovic-debug/control-tower/issues/6), это пока не действующая гарантия.

## Безопасность и данные

Репозиторий **публичный**. Не публикуйте API tokens, пароли, SSH-ключи, `.env`, данные клиентов и партнёров, персональные данные, конфиденциальные документы, raw POS-данные и секреты GitHub Actions. `.gitignore` не заменяет проверку секретов. Для приватных исследований требуется отдельное согласованное хранилище.

**Лицензия пока не выбрана.** Публичная доступность репозитория сама по себе не предоставляет разрешение на использование кода в сторонних продуктах; добавлять LICENSE после явного решения владельца.

## Источник истины

GitHub хранит согласованные и версионированные артефакты. Обсуждение в ChatGPT не означает, что соответствующее решение или документ появился в репозитории. Достоверность утверждений требует ссылок на источники, проверок или релевантных PR/Actions runs. Успешное выполнение CI не подтверждает бизнес-ценность пилота.
