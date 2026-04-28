# Scripts

This directory contains local demo and replay utilities.

## Available scripts

- `seed_demo_data.sh`
  Replays every bundled demo scenario into the running platform.
- `replay_demo_scenario.py`
  Replays a single JSON scenario file into the ingestion API.

## Quick examples

Seed everything:

```bash
./scripts/seed_demo_data.sh
```

Replay one scenario against a custom API host:

```bash
python3 scripts/replay_demo_scenario.py \
  datasets/demo-scenarios/port-scan-incident.json \
  --api-base http://localhost:8080 \
  --delay-ms 150
```
