package com.emotion.emotionService.infrastructure.lexicon;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.Objects;

// TODO: move to actual Vader library instead of demo
@Component
public class VaderLikeAnalyzer {

  private static final Map<String, Double> WORD_SCORES =
      Map.of(
          "good", 2.0,
          "great", 3.0,
          "excellent", 3.5,
          "happy", 2.5,
          "bad", -2.0,
          "terrible", -3.5,
          "awful", -3.0,
          "annoying", -2.5,
          "frustrated", -3.0,
          "angry", -3.5);

  public double sentiment(String text) {

    String[] tokens = normalize(text);

    DoubleSummaryStatistics stats =
        Arrays.stream(tokens)
            .map(WORD_SCORES::get)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .summaryStatistics();

    if (stats.getCount() == 0) {
      return 0.0;
    }

    double raw = stats.getAverage();

    // normalize to [-1,1]
    return Math.tanh(raw / 3.0);
  }

  public double intensity(String text) {

    double intensity = 0.5;

    if (text.contains("!")) intensity += 0.2;

    if (text.equals(text.toUpperCase())) intensity += 0.2;

    if (text.contains("??")) intensity += 0.1;

    return Math.min(intensity, 1.0);
  }

  private String[] normalize(String text) {
    return text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
  }
}
