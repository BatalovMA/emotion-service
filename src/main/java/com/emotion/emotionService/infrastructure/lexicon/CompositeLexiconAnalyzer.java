package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private final NrcEmotionAnalyzer nrc;
  private final DepecheMoodAnalyzer depecheMood;

  @Override
  public LexiconResult analyze(String text) {

    Map<String, Double> combined = new HashMap<>();
    LexiconFusionSupport.mergeScores(combined, nrc.analyzeScores(text), NRC_RELATIVE_WEIGHT);
    LexiconFusionSupport.mergeScores(
        combined, depecheMood.analyzeScores(text), DEPECHE_RELATIVE_WEIGHT);

    combined.replaceAll((key, value) -> Math.max(0.0, value));

    Map<String, Double> normalized = LexiconFusionSupport.normalize(combined);
    double sentiment = LexiconFusionSupport.resolveSentiment(normalized);
    double intensity = LexiconFusionSupport.resolveIntensity(normalized);

    List<String> rankedEmotions =
        normalized.entrySet().stream()
            .filter(entry -> entry.getValue() >= EMOTION_THRESHOLD)
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(MAX_EMOTIONS)
            .map(Map.Entry::getKey)
            .toList();

    if (rankedEmotions.isEmpty()) {
      rankedEmotions = List.of(LexiconFusionSupport.resolveFallbackEmotion(sentiment));
    }

    return LexiconResult.builder()
        .sentiment(sentiment)
        .intensity(intensity)
        .emotions(rankedEmotions)
        .build();
  }
}
