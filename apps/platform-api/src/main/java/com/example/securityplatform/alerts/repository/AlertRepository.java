package com.example.securityplatform.alerts.repository;

import com.example.securityplatform.alerts.domain.Alert;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface AlertRepository extends ListCrudRepository<Alert, UUID> {
}
