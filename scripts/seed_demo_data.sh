#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_BASE="${API_BASE:-http://localhost:8080}"

echo "Seeding bundled demo scenarios into ${API_BASE}"

python3 "${ROOT_DIR}/scripts/replay_demo_scenario.py" "${ROOT_DIR}/datasets/demo-scenarios/brute-force-attack.json" --api-base "${API_BASE}" --delay-ms 50
python3 "${ROOT_DIR}/scripts/replay_demo_scenario.py" "${ROOT_DIR}/datasets/demo-scenarios/port-scan-incident.json" --api-base "${API_BASE}" --delay-ms 50
python3 "${ROOT_DIR}/scripts/replay_demo_scenario.py" "${ROOT_DIR}/datasets/demo-scenarios/error-spike-anomaly.json" --api-base "${API_BASE}" --delay-ms 50

echo "All demo scenarios were replayed."
