package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class CompositeLexiconAnalyzer implements LexiconAnalyzer {

  private static final double NRC_RELATIVE_WEIGHT = 1.0;
  private static final double DEPECHE_RELATIVE_WEIGHT = 1.0;

  private final NrcEmotionAnalyzer nrc;
  private final DepecheMoodAnalyzer depecheMood;
  private final LexiconScoringService scoringService;

  @Override
  public LexiconResult analyze(String text) {

    Map<String, Double> combined = new HashMap<>();
    mergeScores(combined, nrc.analyzeScores(text), NRC_RELATIVE_WEIGHT);
    mergeScores(combined, depecheMood.analyzeScores(text), DEPECHE_RELATIVE_WEIGHT);

    combined.replaceAll((key, value) -> Math.max(0.0, value));

    return scoringService.buildResult(combined, true);
  }

  private void mergeScores(Map<String, Double> target, Map<String, Double> scores, double weight) {
    scores.forEach((emotion, value) -> target.merge(emotion, value * weight, Double::sum));
  }
}
