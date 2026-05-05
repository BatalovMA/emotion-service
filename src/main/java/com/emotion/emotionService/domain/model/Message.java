package com.emotion.emotionService.domain.model;

import java.time.Instant;

@lombok.Value
@lombok.Builder
public class Message {
  String speaker;
  String text;
  Instant timestamp;
}
