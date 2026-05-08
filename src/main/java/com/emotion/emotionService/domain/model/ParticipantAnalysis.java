package com.emotion.emotionService.domain.model;

@lombok.Value
@lombok.Builder
public class ParticipantAnalysis {
  String speaker;
  double temperature;
  String dominantEmotion;
  String emotionalTrend;
}
