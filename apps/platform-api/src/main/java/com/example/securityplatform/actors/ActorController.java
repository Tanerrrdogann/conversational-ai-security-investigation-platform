package com.example.securityplatform.actors;

import com.example.securityplatform.actors.service.ActorQueryService;
import com.example.securityplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
public class ActorController {

    private final ActorQueryService actorQueryService;

    public ActorController(ActorQueryService actorQueryService) {
        this.actorQueryService = actorQueryService;
    }

    @GetMapping
    public ApiResponse<List<ActorResponse>> listActors() {
        return ApiResponse.ok(actorQueryService.list());
    }
}
