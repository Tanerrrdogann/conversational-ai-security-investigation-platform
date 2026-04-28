package com.example.securityplatform.events.repository;

import com.example.securityplatform.events.domain.NormalizedEvent;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface NormalizedEventRepository extends ListCrudRepository<NormalizedEvent, UUID> {
}
