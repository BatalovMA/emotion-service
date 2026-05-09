package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class CompositeLexiconAnalyzer implements LexiconAnalyzer {

  private static final double NRC_RELATIVE_WEIGHT = 1.0;
  private static final double DEPECHE_RELATIVE_WEIGHT = 2.0;
  private static final double EMOTION_THRESHOLD = 0.15;
  private static final int MAX_EMOTIONS = 3;

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

  private final NrcEmotionAnalyzer nrc;
  private final DepecheMoodAnalyzer depecheMood;

  @Override
  public LexiconResult analyze(String text) {

    Map<String, Double> combined = new HashMap<>();
    mergeScores(combined, nrc.analyzeScores(text), NRC_RELATIVE_WEIGHT);
    mergeScores(combined, depecheMood.analyzeScores(text), DEPECHE_RELATIVE_WEIGHT);

    combined.replaceAll((key, value) -> Math.max(0.0, value));

    Map<String, Double> normalized = normalize(combined);
    double sentiment = resolveSentiment(normalized);
    double intensity = resolveIntensity(normalized);

    List<String> rankedEmotions =
        normalized.entrySet().stream()
            .filter(entry -> entry.getValue() >= EMOTION_THRESHOLD)
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(MAX_EMOTIONS)
            .map(Map.Entry::getKey)
            .toList();

    if (rankedEmotions.isEmpty()) {
      String result = "neutral";

      if (sentiment >= 0.3) {
        result = "joy";
      } else if (sentiment <= -0.3) {
        result = "anger";
      }

      rankedEmotions = List.of(result);
    }

    return LexiconResult.builder()
        .sentiment(sentiment)
        .intensity(intensity)
        .emotions(rankedEmotions)
        .build();
  }

  private void mergeScores(Map<String, Double> target, Map<String, Double> scores, double weight) {
    scores.forEach((emotion, value) -> target.merge(emotion, value * weight, Double::sum));
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
}
