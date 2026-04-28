CREATE TABLE IF NOT EXISTS raw_ingested_events (
    ingestion_id UUID PRIMARY KEY,
    source_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    source_ip VARCHAR(64),
    destination_ip VARCHAR(64),
    destination_port INTEGER,
    username VARCHAR(128),
    service_name VARCHAR(128),
    endpoint VARCHAR(255),
    raw_message TEXT NOT NULL,
    processing_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_raw_ingested_events_occurred_at
    ON raw_ingested_events (occurred_at);

CREATE INDEX IF NOT EXISTS idx_raw_ingested_events_source_type
    ON raw_ingested_events (source_type);

CREATE INDEX IF NOT EXISTS idx_raw_ingested_events_processing_status
    ON raw_ingested_events (processing_status);

CREATE TABLE IF NOT EXISTS traffic_events (
    traffic_event_id UUID PRIMARY KEY,
    ingestion_id UUID NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    source_ip VARCHAR(64),
    destination_ip VARCHAR(64),
    destination_port INTEGER,
    service_name VARCHAR(128),
    endpoint VARCHAR(255),
    raw_message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_traffic_events_occurred_at
    ON traffic_events (occurred_at);

CREATE INDEX IF NOT EXISTS idx_traffic_events_source_ip
    ON traffic_events (source_ip);

CREATE TABLE IF NOT EXISTS log_events (
    log_event_id UUID PRIMARY KEY,
    ingestion_id UUID NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    source_ip VARCHAR(64),
    destination_ip VARCHAR(64),
    destination_port INTEGER,
    username VARCHAR(128),
    service_name VARCHAR(128),
    endpoint VARCHAR(255),
    raw_message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_log_events_occurred_at
    ON log_events (occurred_at);

CREATE INDEX IF NOT EXISTS idx_log_events_username
    ON log_events (username);

CREATE TABLE IF NOT EXISTS normalized_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    severity VARCHAR(16) NOT NULL,
    source_ip VARCHAR(64),
    destination_ip VARCHAR(64),
    destination_port INTEGER,
    username VARCHAR(128),
    service_name VARCHAR(128),
    endpoint VARCHAR(255),
    raw_message TEXT NOT NULL,
    normalized_message TEXT NOT NULL,
    tags TEXT,
    correlation_key VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_normalized_events_occurred_at
    ON normalized_events (occurred_at);

CREATE INDEX IF NOT EXISTS idx_normalized_events_event_type
    ON normalized_events (event_type);

CREATE INDEX IF NOT EXISTS idx_normalized_events_source_ip
    ON normalized_events (source_ip);

CREATE INDEX IF NOT EXISTS idx_normalized_events_username
    ON normalized_events (username);

CREATE INDEX IF NOT EXISTS idx_normalized_events_correlation_key
    ON normalized_events (correlation_key);

CREATE TABLE IF NOT EXISTS alerts (
    alert_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    reason TEXT NOT NULL,
    source VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    event_id UUID,
    triggered_rule VARCHAR(128),
    supporting_evidence TEXT,
    anomaly_contribution DOUBLE PRECISION,
    related_incidents TEXT,
    recommendation TEXT,
    acknowledgment_state VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    resolution_state VARCHAR(32) NOT NULL DEFAULT 'UNRESOLVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alerts_event_id
    ON alerts (event_id);

CREATE INDEX IF NOT EXISTS idx_alerts_status
    ON alerts (status);

CREATE TABLE IF NOT EXISTS rule_matches (
    match_id UUID PRIMARY KEY,
    rule_name VARCHAR(128) NOT NULL,
    event_id UUID NOT NULL,
    severity VARCHAR(16) NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rule_matches_event_id
    ON rule_matches (event_id);

CREATE INDEX IF NOT EXISTS idx_rule_matches_rule_name
    ON rule_matches (rule_name);

CREATE TABLE IF NOT EXISTS incidents (
    incident_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    summary TEXT NOT NULL,
    correlation_key VARCHAR(255),
    actor_id UUID,
    incident_score INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_incidents_actor_id
    ON incidents (actor_id);

CREATE INDEX IF NOT EXISTS idx_incidents_correlation_key
    ON incidents (correlation_key);

CREATE TABLE IF NOT EXISTS incident_events (
    incident_event_id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    event_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_incident_events_incident_id
    ON incident_events (incident_id);

CREATE INDEX IF NOT EXISTS idx_incident_events_event_id
    ON incident_events (event_id);

CREATE TABLE IF NOT EXISTS actors (
    actor_id UUID PRIMARY KEY,
    actor_key VARCHAR(255) NOT NULL,
    actor_type VARCHAR(32) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    risk_score INTEGER NOT NULL DEFAULT 0,
    last_seen_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_actors_actor_key
    ON actors (actor_key);

CREATE TABLE IF NOT EXISTS actor_risk_history (
    history_id UUID PRIMARY KEY,
    actor_id UUID NOT NULL,
    risk_score INTEGER NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_actor_risk_history_actor_id
    ON actor_risk_history (actor_id);

CREATE TABLE IF NOT EXISTS anomaly_scores (
    anomaly_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    actor_id UUID,
    incident_id UUID,
    anomaly_score DOUBLE PRECISION NOT NULL,
    model_name VARCHAR(128) NOT NULL,
    feature_summary TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_anomaly_scores_event_id
    ON anomaly_scores (event_id);

CREATE INDEX IF NOT EXISTS idx_anomaly_scores_actor_id
    ON anomaly_scores (actor_id);

CREATE TABLE IF NOT EXISTS host_risk_scores (
    host_risk_id UUID PRIMARY KEY,
    host_identifier VARCHAR(128) NOT NULL,
    risk_score INTEGER NOT NULL,
    reason TEXT NOT NULL,
    last_seen_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_host_risk_scores_host_identifier
    ON host_risk_scores (host_identifier);

CREATE TABLE IF NOT EXISTS user_risk_scores (
    user_risk_id UUID PRIMARY KEY,
    username VARCHAR(128) NOT NULL,
    risk_score INTEGER NOT NULL,
    reason TEXT NOT NULL,
    last_seen_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_risk_scores_username
    ON user_risk_scores (username);

CREATE TABLE IF NOT EXISTS rules (
    rule_id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    source VARCHAR(64) NOT NULL,
    condition_expression TEXT NOT NULL,
    threshold_value INTEGER,
    severity VARCHAR(16) NOT NULL,
    time_window_minutes INTEGER,
    explanation_template TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_rules_name
    ON rules (name);

CREATE TABLE IF NOT EXISTS sources (
    source_id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    ingestion_method VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sources_name
    ON sources (name);

CREATE TABLE IF NOT EXISTS investigation_sessions (
    session_id UUID PRIMARY KEY,
    mode VARCHAR(32) NOT NULL,
    last_intent VARCHAR(64),
    last_target_type VARCHAR(64),
    last_target_id VARCHAR(128),
    last_target_label VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_messages (
    message_id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id
    ON chat_messages (session_id);

CREATE TABLE IF NOT EXISTS recommendations (
    recommendation_id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    target_type VARCHAR(64),
    target_id VARCHAR(128),
    recommendation_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_recommendations_session_id
    ON recommendations (session_id);

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '11111111-1111-1111-1111-111111111111', 'BLACKLIST_IP_ACCESS', 'traffic-analysis', 'blacklist indicator', 1, 'CRITICAL', 5,
       'Source IP matched blacklist intelligence and should be treated as high-confidence malicious activity.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'BLACKLIST_IP_ACCESS');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '22222222-2222-2222-2222-222222222222', 'SUSPICIOUS_ADMIN_ENDPOINT_ACCESS', 'log-analysis', 'admin targeting', 1, 'HIGH', 10,
       'Admin or privileged activity was detected against a sensitive endpoint outside normal expectations.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'SUSPICIOUS_ADMIN_ENDPOINT_ACCESS');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '33333333-3333-3333-3333-333333333333', 'SENSITIVE_PORT_SCAN', 'traffic-analysis', 'sensitive port scan', 2, 'HIGH', 2,
       'The actor targeted one or more sensitive ports in a scan-like pattern.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'SENSITIVE_PORT_SCAN');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '44444444-4444-4444-4444-444444444444', 'ADMIN_LOGIN_FAILURE', 'log-analysis', 'admin login failure', 5, 'HIGH', 5,
       'Repeated failed login activity targeted an administrative or privileged identity.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'ADMIN_LOGIN_FAILURE');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '55555555-5555-5555-5555-555555555555', 'CRITICAL_SERVICE_ERROR_SPIKE', 'log-analysis', 'critical service error spike', 3, 'HIGH', 10,
       'A critical service generated an abnormal spike of errors or exceptions.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'CRITICAL_SERVICE_ERROR_SPIKE');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '66666666-6666-6666-6666-666666666666', 'REPEATED_ACCESS_DENIED', 'log-analysis', 'denied access storm', 3, 'MEDIUM', 5,
       'The actor repeatedly hit denied-access conditions against the same target surface.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'REPEATED_ACCESS_DENIED');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '77777777-7777-7777-7777-777777777777', 'HIGH_404_RESOURCE_PROBING', 'log-analysis', '404 probing', 5, 'MEDIUM', 10,
       'The actor generated elevated missing-resource or 404 behavior that resembles probing.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'HIGH_404_RESOURCE_PROBING');

INSERT INTO rules (rule_id, name, source, condition_expression, threshold_value, severity, time_window_minutes, explanation_template, enabled)
SELECT '88888888-8888-8888-8888-888888888888', 'PRIVILEGED_ACTION_REVIEW', 'log-analysis', 'privileged action', 1, 'HIGH', 15,
       'A privileged action occurred and should be reviewed for legitimacy.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM rules WHERE name = 'PRIVILEGED_ACTION_REVIEW');

INSERT INTO sources (source_id, name, source_type, ingestion_method, description, enabled)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Traffic Sensor Feed', 'TRAFFIC', 'REST',
       'Network-style event ingestion stream used for scan, probe, and port targeting signals.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sources WHERE name = 'Traffic Sensor Feed');

INSERT INTO sources (source_id, name, source_type, ingestion_method, description, enabled)
SELECT 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Authentication Logs', 'AUTH_LOG', 'REST',
       'Authentication and identity events used for failed login and privileged access analysis.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sources WHERE name = 'Authentication Logs');

INSERT INTO sources (source_id, name, source_type, ingestion_method, description, enabled)
SELECT 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Application Logs', 'APP_LOG', 'FILE_UPLOAD',
       'Application telemetry used for error spikes and suspicious endpoint activity.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sources WHERE name = 'Application Logs');

INSERT INTO sources (source_id, name, source_type, ingestion_method, description, enabled)
SELECT 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Scenario Replay Queue', 'SIMULATED_ATTACK', 'QUEUE',
       'Replay-oriented source used for demos, queued attack flows, and scenario seeding.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sources WHERE name = 'Scenario Replay Queue');

INSERT INTO sources (source_id, name, source_type, ingestion_method, description, enabled)
SELECT 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'System Events', 'SYSTEM_EVENT', 'REST',
       'Operating system and host-level telemetry used for security-relevant system event investigations.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sources WHERE name = 'System Events');
