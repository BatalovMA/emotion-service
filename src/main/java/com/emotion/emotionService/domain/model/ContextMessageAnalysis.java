package com.emotion.emotionService.domain.model;

import java.util.UUID;

@lombok.Value
@lombok.Builder
public class ContextMessageAnalysis {
  UUID sessionId;
  MessageAnalysis message;
  double overallTemperature;
  String dominantDialogueEmotion;
  Trajectory trajectory;
}

