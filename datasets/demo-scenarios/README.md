# Demo Scenarios

These datasets are ready-to-replay scenario bundles for the platform.

## Included scenarios

- `brute-force-attack.json`
  Typical admin login abuse flow with repeated failed authentication and denied access noise.
- `port-scan-incident.json`
  Reconnaissance followed by privileged targeting from a single actor.
- `error-spike-anomaly.json`
  Critical service instability mixed with suspicious web probing.

## Event format

Each file contains:

- `name`
- `description`
- `events`

Each event is shaped for the ingestion API:

- `sourceType`
- `occurredAt`
- `sourceIp`
- `destinationIp`
- `destinationPort`
- `username`
- `serviceName`
- `endpoint`
- `rawMessage`

## Usage

Replay one scenario:

```bash
python3 scripts/replay_demo_scenario.py datasets/demo-scenarios/brute-force-attack.json
```

Replay all scenarios:

```bash
./scripts/seed_demo_data.sh
```
