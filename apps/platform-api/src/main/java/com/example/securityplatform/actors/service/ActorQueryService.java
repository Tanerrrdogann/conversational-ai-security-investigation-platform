package com.example.securityplatform.actors.service;

import com.example.securityplatform.actors.ActorResponse;
import com.example.securityplatform.actors.repository.ActorRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ActorQueryService {

    private final ActorRepository actorRepository;

    public ActorQueryService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<ActorResponse> list() {
        return actorRepository.findAll().stream()
                .sorted(Comparator.comparing(actor -> actor.getRiskScore() == null ? 0 : actor.getRiskScore(), Comparator.reverseOrder()))
                .map(actor -> new ActorResponse(
                        actor.getActorId(),
                        actor.getActorKey(),
                        actor.getActorType().name(),
                        actor.getDisplayName(),
                        actor.getRiskScore(),
                        actor.getLastSeenAt(),
                        actor.getCreatedAt()
                ))
                .toList();
    }
}
