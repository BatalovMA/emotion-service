package com.emotion.emotionService.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@lombok.Value
@lombok.Builder
@lombok.extern.jackson.Jacksonized
public class DialogueContext {
  UUID sessionId;
  List<Message> messages;
  Instant updatedAt;
}
