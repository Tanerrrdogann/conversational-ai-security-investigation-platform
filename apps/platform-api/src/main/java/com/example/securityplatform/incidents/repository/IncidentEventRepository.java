package com.example.securityplatform.incidents.repository;

import com.example.securityplatform.incidents.domain.IncidentEvent;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface IncidentEventRepository extends ListCrudRepository<IncidentEvent, UUID> {
}
