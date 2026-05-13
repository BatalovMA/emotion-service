package com.emotion.emotionService.evaluation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GoEmotionsDatasetLoader {

  private static final List<String> LABELS =
      List.of(
          "admiration",
          "amusement",
          "anger",
          "annoyance",
          "approval",
          "caring",
          "confusion",
          "curiosity",
          "desire",
          "disappointment",
          "disapproval",
          "disgust",
          "embarrassment",
          "excitement",
          "fear",
          "gratitude",
          "grief",
          "joy",
          "love",
          "nervousness",
          "optimism",
          "pride",
          "realization",
          "relief",
          "remorse",
          "sadness",
          "surprise",
          "neutral");

  public List<DatasetRecord> load(String resourcePath) {
    List<DatasetRecord> records = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(resourcePath))))) {
      reader
          .lines()
          .filter(line -> !line.isBlank())
          .forEach(
              line -> {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                  return;
                }
                String text = parts[0].trim();
                String labelPart = parts[1].trim();
                Set<String> labels = parseLabels(labelPart);
                records.add(new DatasetRecord(text, labels));
              });
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load dataset: " + resourcePath, e);
    }
    return records;
  }

  private Set<String> parseLabels(String labelPart) {
    if (labelPart.isEmpty()) {
      return Set.of("neutral");
    }
    return java.util.Arrays.stream(labelPart.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(this::labelFromIndex)
        .map(label -> label.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
  }

  private String labelFromIndex(String index) {
    int labelIndex = Integer.parseInt(index);
    if (labelIndex < 0 || labelIndex >= LABELS.size()) {
      return "neutral";
    }
    return LABELS.get(labelIndex);
  }

  public record DatasetRecord(String text, Set<String> labels) {}
}
