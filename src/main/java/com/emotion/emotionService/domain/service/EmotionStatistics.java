package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    return messages.stream()
        .map(MessageAnalysis::getEmotion)
        .filter(emotions -> emotions != null && !emotions.isEmpty())
        .map(List::getFirst)
        .collect(Collectors.groupingBy(emotion -> emotion, Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(fallback);
  }

  public static double averageAbsoluteDelta(List<Double> values) {
    if (values == null || values.size() < 2) {
      return 0.0;
    }

    return java.util.stream.IntStream.range(1, values.size())
        .mapToDouble(i -> Math.abs(values.get(i) - values.get(i - 1)))
        .average()
        .orElse(0.0);
  }

  public static double averageAbsoluteDeltaMessages(List<MessageAnalysis> messages) {
    if (messages == null || messages.size() < 2) {
      return 0.0;
    }

    return java.util.stream.IntStream.range(1, messages.size())
        .mapToDouble(
            i ->
                Math.abs(
                    messages.get(i).getTemperature() - messages.get(i - 1).getTemperature()))
        .average()
        .orElse(0.0);
  }
}
