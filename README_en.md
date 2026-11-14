# Risk Engine（Radar）

## Introduction

A lightweight, real-time risk analysis engine. Risk rules are edited visually and take effect
in real time — no redeploy needed. It applies to anti-fraud scenarios perfectly.
The project is called Radar: like a radar, it monitors every transaction in the background.

Since v2.0.0, Radar is a single-module monolith on **Java 17 + Spring Boot 3.5**:
one jar serves both the admin console and the engine API (default port 6580).

## Feature

 * real-time rule updates, sub-100ms response in common scenarios
 * visual rule editor with rich operators
 * Groovy-powered rule engine — flexible for complex scenarios
 * plugin design (preItems) for fast external data integration
 * NoSQL storage, easy to scale
 * simple configuration, out-of-the-box

## Quick Start (Docker)

Requirements: Docker & Docker Compose.

```bash
# infrastructure only (MySQL / MongoDB / Redis / Elasticsearch, seed SQL auto-imported)
docker compose up -d

# or run the application image as well, then open http://localhost:6580
docker compose --profile app up -d --build
```

Admin UI: http://localhost:6580/  · API docs: http://localhost:6580/swagger-ui/index.html

## Build from Source

Requirements: JDK 17+, Maven.

```bash
mvn package -s .mvn/settings.xml -DskipTests   # or: mvn verify to run all tests (Docker required)
java -jar target/radar.jar
```

Tests are layered: plain unit tests run everywhere (`mvn test`);
integration tests use Testcontainers and run in CI (`env.CI` profile) or locally with `-Pci`.

## Related Sites

    Gitee:  https://gitee.com/freshday/radar   (primary)
    Github: https://github.com/wfh45678/radar  (mirror, CI runs here)
    Official Site:  https://www.riskengine.cn
    Wiki:   https://gitee.com/freshday/radar/wikis/home

## Architecture

Server: Spring Boot 3.5 + MyBatis-Plus + MySQL + MongoDB + Redis + Groovy 4 + Elasticsearch 7 + springdoc-openapi
Front-end: React (SPA, pre-built into the jar)

![Architecture](https://www.riskengine.cn/radar/sys_model_arch.png)

## Technology Stack

* Spring Boot 3.5: application framework (Java 17).
* MyBatis-Plus: persistence with generic CRUD and pagination.
* MySQL: model meta-data storage.
* MongoDB: event storage and basic statistics.
* Elasticsearch: data retrieval and rule-hit reporting.
* Redis: caching and pub/sub for real-time model config updates.
* Groovy 4: rule engine, scripts recompiled and effective immediately.
* springdoc-openapi: REST API docs.

## Docs

* [User manual (中文)](https://gitee.com/freshday/radar/wikis/manual)
* [Upgrade plan: Java 17 + Boot 3](docs/upgrade-plan-java17-boot3.md)
* [Technical roadmap](docs/roadmap.md)
* [Contributing](CONTRIBUTING.md)

## Demo

[Demo URL](https://www.riskengine.cn): https://www.riskengine.cn

## Thanks

千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦
成书平, 徐帅，郭锐， 王成，马兆永...

## Statement

Mulan PSL v2 licensed. Copyright © 2019-2026 WangFeiHu
