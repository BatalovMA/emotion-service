package com.emotion.emotionService.infrastructure.lexicon;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NrcEmotionAnalyzer {

  private final Map<String, Set<String>> wordToEmotions = new HashMap<>();

  @PostConstruct
  public void init() {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/lexicon/nrc.txt"))))) {

      reader
          .lines()
          .map(line -> line.split("\t"))
          .filter(parts -> parts.length >= 3)
          .forEach(
              parts -> {
                String word = parts[0];
                String emotion = parts[1];
                int association = Integer.parseInt(parts[2]);
                if (association == 1) {
                  wordToEmotions.computeIfAbsent(word, k -> new HashSet<>()).add(emotion);
                }
              });
    } catch (Exception e) {
      throw new RuntimeException("Failed to load NRC lexicon", e);
    }
  }

  public Map<String, Double> analyze(String text) {

    Map<String, Double> scores = analyzeScores(text);
    double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();

    if (total == 0) {
      return Collections.emptyMap();
    }

    return scores.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / total));
  }

  Map<String, Double> analyzeScores(String text) {

    return LexiconPreprocessor.tokenizeWithNegation(text).stream()
        .filter(token -> wordToEmotions.containsKey(token.word()))
        .flatMap(
            token ->
                wordToEmotions.get(token.word()).stream()
                    .map(emotion -> token.negated() ? invertPolarity(emotion) : emotion))
        .collect(Collectors.toMap(emotion -> emotion, emotion -> 1.0, Double::sum));
  }

  private String invertPolarity(String emotion) {
    if ("positive".equals(emotion)) {
      return "negative";
    }
    if ("negative".equals(emotion)) {
      return "positive";
    }
    return emotion;
  }
}
