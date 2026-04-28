package com.example.securityplatform.actors.repository;

import com.example.securityplatform.actors.domain.Actor;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ActorRepository extends ListCrudRepository<Actor, UUID> {
}
