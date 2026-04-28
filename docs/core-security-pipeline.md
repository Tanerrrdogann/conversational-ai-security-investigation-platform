# Core Security Pipeline

This document explains the core security pipeline in very simple terms.

## What the pipeline does

When a raw security event enters the system, the platform does not immediately show it as an alert.
It first tries to understand what the event means.

The flow is:

1. Ingestion receives the raw event.
2. Traffic analysis and log analysis read the raw event and produce simple findings.
3. Normalization turns the raw event into a clean, shared security event format.
4. Rule engine checks whether the event matches security rules.
5. Alert generation stores alerts for matched rules.

## Think of it like this

Imagine a security camera, a door sensor, and a login log all send different kinds of messages.
Each one speaks a different language.

This pipeline acts like:

- a receiver that collects the message
- a translator that makes the message consistent
- a checker that asks "is this dangerous?"
- an alarm box that creates an alert if danger is found

## Step 1: Ingestion

Ingestion is the front door of the platform.

It accepts a raw event such as:

- source type
- time
- source IP
- destination IP
- port
- username
- endpoint
- raw message

At this point, the system only knows "something happened."
It does not yet know whether this is a failed login, a scan, or a suspicious admin action.

## Step 2: Traffic analysis

Traffic analysis looks at network-style behavior.

It tries to answer questions like:

- Does this look like scanning?
- Is the target port sensitive?
- Is this source touching admin paths?
- Does the message mention a blacklist or threat intel hit?

It produces small findings, for example:

- "scan-like network behavior"
- "targeting around an admin-facing path"
- "blacklist indicator"

## Step 3: Log analysis

Log analysis looks at application, auth, and access logs.

It tries to answer questions like:

- Is this a failed login?
- Is access being denied?
- Is there an error spike?
- Is this a 404 probing pattern?
- Is a privileged action happening?

It also produces findings that help the next step understand the event better.

## Step 4: Normalization

Normalization means turning different raw inputs into one shared security event model.

For example:

- "failed login for admin account"
- "login failed: admin"
- "authentication denied for admin"

All of these can become one normalized event type like `FAILED_LOGIN`.

This is important because the rest of the system should not care how the original message was written.
It should work with one clean event structure.

## Step 5: Rule engine

The rule engine is the part that checks:

"Does this event match one of our security rules?"

Examples:

- sensitive port scan
- admin login failure
- critical service error spike
- repeated access denied
- privileged action review

If a rule matches, the system records a rule match and creates an alert.

## Step 6: Alert generation

An alert is the system saying:

"This event is important enough to investigate."

The alert stores things like:

- title
- severity
- reason
- source
- related event id
- triggered rule

So instead of only storing raw text, the platform stores a structured explanation of why the event matters.

## Why this matters

Without this pipeline, the platform is only a place where logs are dumped.

With this pipeline:

- raw events become understandable
- different sources become comparable
- security rules become explainable
- alerts become consistent

That is the foundation we need before building the deeper AI investigation layer on top.
