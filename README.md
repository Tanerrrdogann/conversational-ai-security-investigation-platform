# Conversational AI Security Investigation Platform

An AI-first security investigation platform that ingests security events, normalizes them, scores risk, correlates suspicious behavior, and lets an analyst investigate through a conversational interface.

## Projeyi Yapanlar

- Ismail Taner Erdogan
- Nisa Goksen

## What It Does

- Collects security events through an ingestion API
- Normalizes mixed raw inputs into a shared event model
- Applies traffic analysis and log analysis
- Triggers rule-backed alerts
- Stores dedicated `traffic_events` and `log_events` alongside raw and normalized data
- Scores anomalies with an Isolation Forest-based AI service
- Correlates related activity into incidents
- Tracks actor risk history
- Produces first-class host and user risk scores
- Answers investigation questions in a grounded AI chat workflow
- Supports real OpenAI-compatible LLM responses with grounded fallback mode
- Presents everything in a dashboard and research workspace

## Architecture

- `apps/platform-api`
  Spring Boot platform for ingestion, normalization, rules, actors, alerts, incidents, and investigation orchestration
- `apps/ai-service`
  FastAPI service for anomaly scoring and grounded investigation response generation
- `apps/web`
  React investigation workspace with auth, overview, event stream, alerts, incidents, actors, anomalies, rules, sources, host/user risk context, scenario replay, and AI chat
- `infra/postgres/init`
  PostgreSQL schema bootstrap
- `datasets/demo-scenarios`
  Replayable bundled demo scenarios
- `scripts`
  Seed and replay utilities

## Local Run

```bash
cp .env.example .env
docker compose up --build
```

Then open:

- `http://localhost:3000` for the UI
- `http://localhost:8080` for the platform API
- `http://localhost:8000/docs` for the AI service OpenAPI page

Default credentials:

- Analyst: `analyst / analyst123`
- Admin: `admin / admin123`

These credentials are for local demos only. Change `APP_AUTH_ADMIN_PASSWORD`,
`APP_AUTH_ANALYST_PASSWORD`, and `POSTGRES_PASSWORD` before exposing the stack
outside your machine.

Live LLM mode is enabled when a provider API key is present in `.env`.

Free online option with Groq:

```bash
LLM_PROVIDER=groq
GROQ_API_KEY=your-groq-key
GROQ_MODEL=llama-3.1-8b-instant
GROQ_BASE_URL=https://api.groq.com/openai/v1
```

OpenAI option:

```bash
LLM_PROVIDER=openai
OPENAI_API_KEY=your-key
OPENAI_MODEL=gpt-4o-mini
```

Without a key, the platform stays usable through the grounded fallback response path.

## Demo Flow

Seed all bundled demo scenarios:

```bash
./scripts/seed_demo_data.sh
```

Then sign in and try these questions in the AI chat:

- `Which IP is the riskiest right now?`
- `Why did this alert trigger?`
- `Are these events connected?`
- `Why is 185.24.91.18 risky?`
- `Write a short non-technical summary.`

Full walkthrough:

- [Demo Guide](docs/demo-guide.md)

## Key Modules

1. Data ingestion
2. Event normalization
3. Traffic analysis
4. Log analysis
5. Rule engine
6. Anomaly detection
7. Correlation and risk scoring
8. Alerting
9. AI investigation conversation
10. Dashboard and research panel

## Demo Assets

- [Architecture](docs/architecture.md)
- [Core Security Pipeline](docs/core-security-pipeline.md)
- [Dashboard Preview](docs/screenshots/app/overview-admin.png)
- [AI Chat Preview](docs/screenshots/app/ai-chat-admin.png)
- [Full Screenshot Gallery](docs/screenshots/README.md)

## Current Completion Status

This repo now covers the full AI-first target defined for the project:

- event ingestion through single, queue, and file-upload flows
- dedicated `traffic_events` and `log_events` persistence
- normalized event storage with tags
- traffic and log analysis
- rule engine plus managed rule catalog
- anomaly detection and incident correlation
- actor history and risk scoring plus first-class host and user risk outputs
- enriched alerts with supporting evidence, anomaly contribution, related incidents, recommendation, acknowledgment state, and resolution state
- durable investigation sessions, messages, and recommendations
- grounded AI explanations with optional live OpenAI-compatible model integration
- protected dashboard, event stream, rules, sources, and AI investigation workspace
