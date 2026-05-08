package com.emotion.emotionService.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ContextMessageRequestDto(
    UUID sessionId,
    @NotBlank String speaker,
    @NotBlank String text
) {}

