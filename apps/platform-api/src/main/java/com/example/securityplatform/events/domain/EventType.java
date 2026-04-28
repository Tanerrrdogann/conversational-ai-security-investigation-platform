package com.example.securityplatform.events.domain;

public enum EventType {
    FAILED_LOGIN,
    PORT_SCAN_ATTEMPT,
    BLACKLIST_IP_HIT,
    ACCESS_DENIED_STORM,
    SUSPICIOUS_ADMIN_ACCESS,
    ERROR_SPIKE,
    PRIVILEGED_ACTION,
    HIGH_404_RATE
}
