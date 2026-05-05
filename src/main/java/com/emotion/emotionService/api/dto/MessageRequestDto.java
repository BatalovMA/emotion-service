package com.emotion.emotionService.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@lombok.Data
public class MessageRequestDto {
  @NotBlank private String speaker;

  @NotBlank private String text;

  private Instant timestamp;
}
