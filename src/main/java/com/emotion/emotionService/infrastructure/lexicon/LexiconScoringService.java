package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LexiconScoringService {

  private static final double EMOTION_THRESHOLD = 0.30;
  private static final int MAX_EMOTIONS = 3;
  private static final Set<String> EXCLUDED_EMOTIONS =
      Set.of("trust", "anticipation", "positive", "negative", "surprise");

  private static final Map<String, String> EMOTION_NORMALIZATION =
      Map.ofEntries(
          Map.entry("annoyance", "anger"),
          Map.entry("annoyed", "anger"),
          Map.entry("angry", "anger"),
          Map.entry("disapproval", "anger"),
          Map.entry("sad", "sadness"),
          Map.entry("grief", "sadness"),
          Map.entry("disappointment", "sadness"),
          Map.entry("remorse", "sadness"),
          Map.entry("embarrassment", "sadness"),
          Map.entry("nervousness", "fear"),
          Map.entry("afraid", "fear"),
          Map.entry("amusement", "joy"),
          Map.entry("amused", "joy"),
          Map.entry("excitement", "joy"),
          Map.entry("love", "joy"),
          Map.entry("happy", "joy"),
          Map.entry("dont_care", "neutral"),
          Map.entry("realization", "neutral"),
          Map.entry("confusion", "neutral"));

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

  public LexiconResult buildResult(Map<String, Double> scores, boolean normalize) {
    if (scores == null || scores.isEmpty()) {
      return neutralResult();
    }

    Map<String, Double> normalizedScores = normalizeLabels(scores);
    Map<String, Double> normalized = normalize ? normalize(normalizedScores) : normalizedScores;
    if (normalized.isEmpty()) {
      return neutralResult();
    }

    double sentiment = resolveSentiment(normalized);
    double intensity = resolveIntensity(normalized);

    Map<String, Double> rankedScores =
        normalized.entrySet().stream()
            .filter(entry -> !EXCLUDED_EMOTIONS.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    List<String> rankedEmotions =
        rankedScores.entrySet().stream()
            .filter(entry -> entry.getValue() >= EMOTION_THRESHOLD)
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(MAX_EMOTIONS)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    if (rankedEmotions.isEmpty()) {
      rankedEmotions = List.of(resolveFallbackEmotion(sentiment));
    }

    return LexiconResult.builder()
        .sentiment(sentiment)
        .intensity(intensity)
        .emotions(rankedEmotions)
        .build();
  }

  private Map<String, Double> normalizeLabels(Map<String, Double> scores) {
    return scores.entrySet().stream()
        .collect(
            Collectors.toMap(
                entry -> normalizeLabel(entry.getKey()), Map.Entry::getValue, Double::sum));
  }

  private String normalizeLabel(String rawLabel) {
    if (rawLabel == null || rawLabel.isBlank()) {
      return "neutral";
    }
    String key = rawLabel.toLowerCase().trim();
    return EMOTION_NORMALIZATION.getOrDefault(key, key);
  }

  private LexiconResult neutralResult() {
    return LexiconResult.builder()
        .sentiment(0.0)
        .intensity(0.0)
        .emotions(List.of("neutral"))
        .build();
  }

  private Map<String, Double> normalize(Map<String, Double> scores) {
    double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
    if (total == 0) {
      return Map.of();
    }
    return scores.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / total));
  }

  private double resolveSentiment(Map<String, Double> emotions) {
    return emotions.entrySet().stream()
        .mapToDouble(entry -> entry.getValue() * EMOTION_POLARITY.getOrDefault(entry.getKey(), 0.0))
        .sum();
  }

  private double resolveIntensity(Map<String, Double> emotions) {
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

  private String resolveFallbackEmotion(double sentiment) {
    if (sentiment >= 0.3) {
      return "joy";
    }
    if (sentiment <= -0.3) {
      return "anger";
    }
    return "neutral";
  }
}
