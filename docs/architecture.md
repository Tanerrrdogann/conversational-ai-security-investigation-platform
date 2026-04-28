# Architecture Overview

The platform is organized as a polyglot monorepo:

- Spring Boot handles ingestion, normalization, rules, alerts, incidents, and investigation orchestration.
- FastAPI handles anomaly scoring and explanation generation.
- React provides analyst-facing dashboards and chat.
- PostgreSQL stores platform state.
- Redis is reserved for session context and caching.

