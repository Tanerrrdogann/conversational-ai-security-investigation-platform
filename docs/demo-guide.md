# Demo Guide

This guide walks through a full end-to-end demo of the platform.

## 1. Start the stack

```bash
cp .env.example .env
docker compose up --build
```

Wait for these services:

- `platform-api` on `http://localhost:8080`
- `ai-service` on `http://localhost:8000`
- `web` on `http://localhost:3000`

## 2. Seed demo data

In a second terminal:

```bash
./scripts/seed_demo_data.sh
```

That replays:

- brute-force attack
- port scan to incident chain
- critical service error spike

## 3. Open the UI

Open:

```text
http://localhost:3000
```

Sign in with:

- analyst: `analyst / analyst123`
- admin: `admin / admin123`

The overview should show live alerts, incidents, actors, anomaly records, managed rules, and registered sources.

## 4. Walk through the main pages

### Overview

- Show the top risky actor card.
- Show the peak anomaly card.
- Open one incident from the priority queue.

### Alerts

- Open the latest alert.
- Trigger "Explain Alert".

### Incidents

- Open the highest-score incident.
- Trigger "Summarize".

### Actors

- Open the top risky actor.
- Trigger "Investigate Actor".

### Anomalies

- Show the highest anomaly score and model summary.

### Rules

- Open the managed rules page.
- As admin, disable and re-enable one rule.

### Sources

- Open the sources page.
- As admin, create a demo source entry.

### Replay

- Return to overview.
- Replay a queue batch or upload one scenario JSON file.

## 5. Run the AI scenarios

Use the AI chat panel with these prompts.

### Scenario 1: Brute-force alert

```text
Why did this alert trigger?
```

### Scenario 2: Correlated incident

```text
Are these events connected?
```

### Scenario 3: Error spike analysis

```text
Is this only a system error, or could it indicate an attack?
```

### Scenario 4: Risky actor history

```text
Why is 185.24.91.18 risky?
```

Then follow with:

```text
Is this connected to earlier activity?
```

### Scenario 5: Executive summary

Switch UI mode to `Plain` and ask:

```text
Write a short non-technical summary.
```

## 6. Screenshot assets

Static preview assets live here:

- [overview-admin.png](screenshots/app/overview-admin.png)
- [ai-chat-admin.png](screenshots/app/ai-chat-admin.png)
- [full screenshot gallery](screenshots/README.md)

## 7. Suggested demo flow

1. Seed the scenarios.
2. Show alerts and incidents being created.
3. Pivot from top risky actor to incident.
4. Ask the copilot why the alert matters.
5. Ask a follow-up question to show memory.
6. Switch from technical mode to plain mode to show explanation control.
7. Show the rules and sources control plane as admin.
