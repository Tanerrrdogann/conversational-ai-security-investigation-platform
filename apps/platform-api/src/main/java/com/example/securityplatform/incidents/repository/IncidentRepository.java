package com.example.securityplatform.incidents.repository;

import com.example.securityplatform.incidents.domain.Incident;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface IncidentRepository extends ListCrudRepository<Incident, UUID> {
}
