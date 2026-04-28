#!/usr/bin/env python3
"""Replay a demo scenario JSON file into the platform ingestion API."""

from __future__ import annotations

import argparse
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


def basic_auth_header(username: str, password: str) -> str:
    token = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
    return f"Basic {token}"


def post_event(api_base: str, payload: dict[str, object], auth_header: str) -> None:
    body = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        f"{api_base.rstrip('/')}/api/ingestion/events",
        data=body,
        headers={"Content-Type": "application/json", "Authorization": auth_header},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=15) as response:
        if response.status >= 300:
            raise RuntimeError(f"Unexpected status {response.status}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Replay a demo scenario into the platform.")
    parser.add_argument("scenario", help="Path to a demo scenario JSON file")
    parser.add_argument("--api-base", default="http://localhost:8080", help="Platform API base URL")
    parser.add_argument("--delay-ms", type=int, default=100, help="Delay between events in milliseconds")
    parser.add_argument("--username", default=os.getenv("API_USERNAME", os.getenv("APP_AUTH_ANALYST_USERNAME", "analyst")), help="Basic auth username")
    parser.add_argument("--password", default=os.getenv("API_PASSWORD", os.getenv("APP_AUTH_ANALYST_PASSWORD", "analyst123")), help="Basic auth password")
    args = parser.parse_args()

    scenario_path = Path(args.scenario)
    scenario = json.loads(scenario_path.read_text(encoding="utf-8"))
    events = scenario.get("events", [])

    print(f"Replaying scenario: {scenario.get('name', scenario_path.name)}")
    print(f"Description: {scenario.get('description', 'No description')}")
    print(f"Target API: {args.api_base}")
    print(f"Event count: {len(events)}")

    auth_header = basic_auth_header(args.username, args.password)
    for index, event in enumerate(events, start=1):
        try:
            post_event(args.api_base, event, auth_header)
            print(f"[{index}/{len(events)}] ingested {event.get('sourceType')} -> {event.get('rawMessage')}")
        except urllib.error.URLError as error:
            print(f"Failed to ingest event {index}: {error}", file=sys.stderr)
            return 1
        time.sleep(max(args.delay_ms, 0) / 1000)

    print("Scenario replay completed successfully.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
