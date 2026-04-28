package com.example.securityplatform.events.repository;

import com.example.securityplatform.events.domain.RawIngestedEvent;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface RawIngestedEventRepository extends ListCrudRepository<RawIngestedEvent, UUID> {
}
