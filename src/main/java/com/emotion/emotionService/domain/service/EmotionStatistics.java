package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EmotionStatistics {

  private EmotionStatistics() {}

  public static double averageTemperature(List<MessageAnalysis> messages) {
    if (messages == null || messages.isEmpty()) {
      return 0.0;
    }

    return messages.stream().mapToDouble(MessageAnalysis::getTemperature).average().orElse(0.0);
  }

  public static String findDominantEmotion(List<MessageAnalysis> messages, String fallback) {
    if (messages == null || messages.isEmpty()) {
      return fallback;
    }

    Map<String, Integer> counts = new HashMap<>();
    for (MessageAnalysis analysis : messages) {
      if (analysis.getEmotion() == null || analysis.getEmotion().isEmpty()) {
        continue;
      }
      String emotion = analysis.getEmotion().get(0);
      counts.merge(emotion, 1, Integer::sum);
    }

    return counts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(fallback);
  }

  public static double averageAbsoluteDelta(List<Double> values) {
    if (values == null || values.size() < 2) {
      return 0.0;
    }

    double sum = 0.0;
    for (int i = 1; i < values.size(); i++) {
      sum += Math.abs(values.get(i) - values.get(i - 1));
    }
    return sum / (values.size() - 1);
  }

  public static double averageAbsoluteDeltaMessages(List<MessageAnalysis> messages) {
    if (messages == null || messages.size() < 2) {
      return 0.0;
    }

    double sum = 0.0;
    for (int i = 1; i < messages.size(); i++) {
      sum += Math.abs(messages.get(i).getTemperature() - messages.get(i - 1).getTemperature());
    }
    return sum / (messages.size() - 1);
  }
}
