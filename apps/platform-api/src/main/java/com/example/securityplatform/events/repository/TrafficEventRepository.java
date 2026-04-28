package com.example.securityplatform.events.repository;

import com.example.securityplatform.events.domain.TrafficEvent;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface TrafficEventRepository extends ListCrudRepository<TrafficEvent, UUID> {
}
