# Risk Engine (Radar)

A lightweight real-time risk decision engine built with Java (Spring Boot 3.5 + MySQL + MongoDB +
Redis + Groovy + ES), designed for anti-fraud and consumer-finance scenarios: rules are edited
visually and take effect immediately — no redeploy needed.
The project is called Radar: like a radar, it monitors every transaction in the background.

[中文](README.md) · [Demo](https://www.riskengine.cn) · [User manual (中文)](https://gitee.com/freshday/radar/wikis/manual)

## Features

* real-time risk decisions, sub-100ms response in common scenarios
* visual rule editor with rich operators
* Multiple strategy types (v4.0): **rule sets** (Groovy condition summing) / **scorecards** (feature binning & weighting) / **decision trees** (path decisions) — extensible via the strategy executor SPI; decision tables & list matching on the roadmap
* Unified feature engineering (v4.1): **enrichment** (chained plugin processing) / **aggregation** (10 time-window aggregates) / **derived expressions** / **ratios** — extensible via the feature executor SPI; rules & strategies reference features uniformly as features.<name>
* Groovy-powered rule engine — scripts recompiled and effective immediately
* plugin ecosystem (v3.0: external jars with dynamic forms)
* single-module monolith (Java 17 + Spring Boot 3.5): one jar serves admin console + engine API

## Quick Start

Requirements: Docker & Docker Compose, or JDK 21+ / Maven / Node 22+ for source build.

```bash
# infrastructure only (MySQL / MongoDB / Redis / Elasticsearch, seed SQL auto-imported)
docker compose up -d

# or build from source (Maven config: .mvn/settings.xml), middleware from the command above
mvn package -s .mvn/settings.xml -DskipTests
java -jar target/radar.jar

# or run the application image as well
docker compose --profile app up -d --build

# frontend dev mode (React SPA in frontend/, /services proxied to :6580)
cd frontend && npm ci && npm run dev
```

Admin UI: http://localhost:6580/ · API docs: http://localhost:6580/swagger-ui/index.html

* Plugin dev: [guide](docs/plugin-dev-guide.md) · [sample project](samples/radar-plugin-demo/README.md)
* Observability: `/actuator/prometheus` metrics, `docker compose --profile monitoring` for a Grafana dashboard
* Tests: plain unit tests run everywhere (`mvn test`); Testcontainers integration tests need Docker, see [CONTRIBUTING.md](CONTRIBUTING.md)

## Architecture

Front/back separated. Server: Spring Boot 3.5 + MyBatis-Plus + MySQL + MongoDB + Redis + Groovy 4 +
Elasticsearch 7 + springdoc-openapi. Front-end: React 18 + antd 5 + Vite (SPA, sources in
[frontend/](frontend/README.md), built into the jar).

> Since v2.0 the 8 Maven modules were merged into a single-module monolith; persistence moved to
>  [roadmap](docs/roadmap.md).

## Community vs Enterprise

Radar ships in two editions: the community edition (this repo) focuses on the core decision engine
and is free under Mulan PSL v2. The enterprise edition targets production deployments for
lending / consumer-finance platforms and **includes everything in the community edition**, plus:

* visual decision-flow orchestration (DAG) and a feature store with real-time computation
* multi-tenancy + RBAC + API keys for SaaS deployments
* business dashboards, SLA alerting via WeCom / email
* model versioning & A/B testing
* professional support with SLA

Full comparison & contact: <https://www.riskengine.cn>

## Docs & Resources

| Resource | Link |
| --- | --- |
| Official site / Demo | <https://www.riskengine.cn> |
| User manual (中文) | <https://gitee.com/freshday/radar/wikis/manual> |
| Wiki home | <https://gitee.com/freshday/radar/wikis/home> |
| Gitee (primary, PRs go here) | <https://gitee.com/freshday/radar> |
| GitHub (mirror) | <https://github.com/wfh45678/radar> |

## Thanks

千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦,
成书平, 徐帅，郭锐， 王成，马兆永…

## Statement

Mulan PSL v2 licensed. Copyright © 2019-2026 WangFeiHu
