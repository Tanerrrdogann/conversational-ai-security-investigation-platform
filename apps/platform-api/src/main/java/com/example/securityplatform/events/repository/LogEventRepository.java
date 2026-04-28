package com.example.securityplatform.events.repository;

import com.example.securityplatform.events.domain.LogEvent;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface LogEventRepository extends ListCrudRepository<LogEvent, UUID> {
}
