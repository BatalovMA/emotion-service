package com.emotion.emotionService.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequestDto(
    @NotBlank String speaker,
    @NotBlank String text
) {}
