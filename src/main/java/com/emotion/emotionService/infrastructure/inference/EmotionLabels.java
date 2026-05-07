package com.emotion.emotionService.infrastructure.inference;

import java.util.List;

public enum EmotionLabels {
  ADMIRATION,
  AMUSEMENT,
  ANGER,
  ANNOYANCE,
  APPROVAL,
  CARING,
  CONFUSION,
  CURIOSITY,
  DESIRE,
  DISAPPOINTMENT,
  DISAPPROVAL,
  DISGUST,
  EMBARRASSMENT,
  EXCITEMENT,
  FEAR,
  GRATITUDE,
  GRIEF,
  JOY,
  LOVE,
  NERVOUSNESS,
  OPTIMISM,
  PRIDE,
  REALIZATION,
  RELIEF,
  REMORSE,
  SADNESS,
  SURPRISE,
  NEUTRAL;

  public static final List<EmotionLabels> LABELS = List.of(values());

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
