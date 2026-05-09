package com.emotion.emotionService.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@lombok.Builder
public record DialogueContext(UUID sessionId, List<Message> messages, Instant updatedAt) {}
