package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Primary
@RequiredArgsConstructor
public class CompositeLexiconAnalyzer implements LexiconAnalyzer {

  private final NrcEmotionAnalyzer nrc;

  @Override
  public LexiconResult analyze(String text) {

    Map<String, Double> emotions = nrc.analyze(text);
    double sentiment = resolveSentiment(emotions);
    double intensity = resolveIntensity(emotions);

    List<String> rankedEmotions =
        emotions.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();

    if (rankedEmotions.isEmpty()) {
      rankedEmotions = List.of(resolveFallbackEmotion(sentiment));
    }

    return LexiconResult.builder()
        .sentiment(sentiment)
        .intensity(intensity)
        .emotions(rankedEmotions)
        .build();
  }

  private double resolveSentiment(Map<String, Double> emotions) {
    double positive = emotions.getOrDefault("positive", 0.0);
    double negative = emotions.getOrDefault("negative", 0.0);
    return positive - negative;
  }

  private double resolveIntensity(Map<String, Double> emotions) {
    double positive = emotions.getOrDefault("positive", 0.0);
    double negative = emotions.getOrDefault("negative", 0.0);
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
