package com.emotion.emotionService.domain.model;

@lombok.Value
@lombok.Builder
public class EmotionResult {
  String speaker;
  double sentiment;
  String emotion;
  double intensity;
  double confidence;
}
