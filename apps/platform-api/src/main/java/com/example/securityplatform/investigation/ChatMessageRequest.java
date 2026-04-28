package com.example.securityplatform.investigation;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String message,
        String sessionId,
        String mode
) {
}
