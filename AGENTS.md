# Instructions for coding agents — PACT

This repository is the versioned source of agreed project artifacts. Chat conversations, unmerged branches and plans are **not** evidence of an implemented feature.

## 1. Before changing anything

- Read this file, the root README, the assigned GitHub Issue, and relevant `docs/architecture/adr/` decisions.
- Inspect the **actual** repository tree and build scripts. Do not assume directories or services mentioned in plans already exist.
- Identify the requested scope, acceptance criteria, dependencies and unresolved assumptions.
- When instructions conflict, describe the conflict in the PR rather than quietly inventing a policy.

## 2. Product and architectural boundaries

- PACT explores a digital model of real-world objects, relationships, states, events, processes and controlled actions. This is an architectural **hypothesis**, not a finished ontology.
- First pilot domain: coffee shops in Ghana; validate proposed universal abstractions also against RAS fish farms when domain modelling is requested.
- Distinguish `Vision`, `Hypothesis`, `Decision`, and verified `Implementation`. Label assumptions and uncertain data explicitly.
- Follow [ADR-0001](docs/architecture/adr/0001-bootstrap-stack.md) for the bootstrap technology baseline and [ADR-0002](docs/architecture/adr/0002-sdlc-delivery-pipeline.md) for the accepted delivery architecture (implementation pending).
- Do **not** introduce microservices, Kubernetes, Kafka, event sourcing, generic graph databases, workflow engines, DSLs, tenant architecture or an object metamodel without a problem-specific decision.
- Backend is a single Spring Boot deployable, organized by coherent features/packages; not multiple separately deployed services unless an ADR changes that.
- No speculative extensibility. Prefer the smallest vertical slice with explicit tests.

## 3. Git workflow

- Start from the assigned Issue. Make focused, short-lived branches and Pull Requests to `main`. Prefer small commits with descriptive messages.
- Keep research and product code changes in distinct PRs unless a concrete experiment requires both and the coupling is explained.
- Production changes must include the right tests and any necessary operational documentation.
- Architecture-affecting changes must link an existing ADR, or propose a new/revised ADR with context, alternatives, consequences and review conditions. An agent cannot silently redefine accepted architecture.
- Do not merge PRs or rewrite history as an unrequested side effect.

## 4. Testing and delivery

- Primary coding/test environment is headless **ubuntu-dev** over SSH; Windows/IntelliJ is for review/UI via SSH tunnel. **ubuntu-staging** is runtime-only: never build code, run Codex or install a self-hosted CI runner there.
- Run applicable local checks defined by the **actual** projects: current backend `cd apps/backend && ./mvnw -B -ntp verify` (Docker/Testcontainers needed); frontend `cd apps/frontend && npm ci && npm run lint && npm run typecheck && npm run test && npm run build`; relevant Compose changes `docker compose config --quiet` and `./scripts/smoke-compose.sh`. Do not claim checks passed without execution.
- The planned `scripts/check-fast.sh` / `scripts/check-full.sh` (#19) and Playwright Chromium `test:e2e` (#20) **do not exist yet**. When introduced, prefer fast checks after small code changes and full/applicable checks before PR. Browser E2E must use a real HTTP stack, not UI-only mocks.
- CI should use path-aware jobs: research/docs-only edits must not trigger heavyweight build/release/deploy. This is a requirement of [Issue #6](https://github.com/felikssladkovic-debug/control-tower/issues/6), **not yet implemented**.
- Never report a test as passed if it was not executed. List command, result, and limitations; explicitly state when tests require Docker, secrets or unavailable infrastructure.
- A green CI build is not operational verification. Independent CI is the required quality gate; docs/research-only changes should not trigger expensive jobs. Release produces immutable, versioned GHCR artifacts pinned by digest; a separately authorized staging deployment only pulls images, verifies HTTPS/health/version and preserves rollback evidence. Database migration rollback is not automatic.
- Do **not** trigger a release, push to production, alter live data, provision cloud resources or grant credentials without explicit user authorization. Production access requires approved controls.

## 5. Security and research quality

- The repository is public. Never commit secrets, production `.env`, identifying data, partner documents, raw financial/POS datasets or other sensitive material. Don't log secrets.
- Use example placeholders and document required environment variables without real values.
- For research: distinguish source facts, interpretations, hypotheses and counterexamples; record origin, date, freshness and confidence. Don't fabricate citations.
- For experiments: define baseline, method, evidence, failure criteria and reproducibility. Label unexecuted protocols as planned.
- Don't copy proprietary Foundry UI assets or assume Blueprint (React) is a Vue dependency.

## 6. Definition of done and PR handoff

- Address every applicable acceptance criterion with evidence; identify anything blocked or out of scope.
- Update documentation only when behavior actually changes. Keep `README.md` status (planned/implemented/verified) accurate.
- Provide links to related Issues, ADRs, tests and runs. For docs-only PRs, explicitly indicate why code CI is unnecessary.
- Prefer one tested working implementation over a large framework skeleton.
