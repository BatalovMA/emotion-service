package com.emotion.emotionService.infrastructure.lexicon;

import java.util.Map;
import java.util.stream.Collectors;

final class LexiconFusionSupport {

  private static final Map<String, Double> EMOTION_POLARITY =
      Map.ofEntries(
          Map.entry("positive", 1.0),
          Map.entry("negative", -1.0),
          Map.entry("joy", 0.7),
          Map.entry("love", 0.7),
          Map.entry("admiration", 0.7),
          Map.entry("approval", 0.7),
          Map.entry("gratitude", 0.7),
          Map.entry("optimism", 0.7),
          Map.entry("relief", 0.7),
          Map.entry("pride", 0.7),
          Map.entry("excitement", 0.7),
          Map.entry("anger", -0.7),
          Map.entry("annoyance", -0.7),
          Map.entry("disappointment", -0.7),
          Map.entry("disapproval", -0.7),
          Map.entry("disgust", -0.7),
          Map.entry("fear", -0.7),
          Map.entry("grief", -0.7),
          Map.entry("remorse", -0.7),
          Map.entry("sadness", -0.7),
          Map.entry("embarrassment", -0.7),
          Map.entry("nervousness", -0.7),
          Map.entry("happy", 0.7),
          Map.entry("amused", 0.7),
          Map.entry("inspired", 0.7),
          Map.entry("angry", -0.7),
          Map.entry("annoyed", -0.7),
          Map.entry("sad", -0.7),
          Map.entry("afraid", -0.7),
          Map.entry("dont_care", 0.0));

  private LexiconFusionSupport() {}

  static void mergeScores(Map<String, Double> target, Map<String, Double> scores, double weight) {
    scores.forEach((emotion, value) -> target.merge(emotion, value * weight, Double::sum));
  }

  static Map<String, Double> normalize(Map<String, Double> scores) {
    double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
    if (total == 0) {
      return Map.of();
    }
    return scores.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / total));
  }

  static double resolveSentiment(Map<String, Double> emotions) {
    return emotions.entrySet().stream()
        .mapToDouble(
            entry -> entry.getValue() * EMOTION_POLARITY.getOrDefault(entry.getKey(), 0.0))
        .sum();
  }

  static double resolveIntensity(Map<String, Double> emotions) {
    double positive =
        emotions.entrySet().stream()
            .filter(entry -> EMOTION_POLARITY.getOrDefault(entry.getKey(), 0.0) > 0)
            .mapToDouble(Map.Entry::getValue)
            .sum();
    double negative =
        emotions.entrySet().stream()
            .filter(entry -> EMOTION_POLARITY.getOrDefault(entry.getKey(), 0.0) < 0)
            .mapToDouble(Map.Entry::getValue)
            .sum();
    return Math.min(1.0, positive + negative);
  }

  static String resolveFallbackEmotion(double sentiment) {
    if (sentiment >= 0.3) {
      return "joy";
    }

    if (sentiment <= -0.3) {
      return "anger";
    }

    return "neutral";
  }
}

