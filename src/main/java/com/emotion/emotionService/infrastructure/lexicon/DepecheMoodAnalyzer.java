package com.emotion.emotionService.infrastructure.lexicon;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DepecheMoodAnalyzer {

  private final Map<String, double[]> wordToScores = new HashMap<>();
  private List<String> emotions = List.of();

  @PostConstruct
  public void init() {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/lexicon/depecheMood.tsv"))))) {

      String header = reader.readLine();
      if (header == null || header.isBlank()) {
        throw new IllegalStateException("DepecheMood header missing");
      }

      String[] columns = header.split("\t");
      if (columns.length < 3) {
        throw new IllegalStateException("DepecheMood header has insufficient columns");
      }

      emotions =
          java.util.Arrays.stream(columns)
              .skip(1)
              .filter(column -> !"freq".equalsIgnoreCase(column))
              .map(column -> column.toLowerCase())
              .toList();

      reader
          .lines()
          .filter(line -> !line.isBlank())
          .map(line -> line.split("\t"))
          .filter(parts -> parts.length >= emotions.size() + 1)
          .forEach(
              parts -> {
                String word = parts[0];
                double[] scores =
                    java.util.stream.IntStream.range(0, emotions.size())
                        .mapToDouble(index -> Double.parseDouble(parts[index + 1]))
                        .toArray();
                wordToScores.put(word, scores);
              });
    } catch (Exception e) {
      throw new RuntimeException("Failed to load DepecheMood lexicon", e);
    }
  }

  public Map<String, Double> analyzeScores(String text) {
    List<LexiconPreprocessor.Token> tokens = LexiconPreprocessor.tokenizeWithNegation(text);
    if (tokens.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, Double> scores = new HashMap<>();
    for (LexiconPreprocessor.Token token : tokens) {
      double[] values = wordToScores.get(token.word());
      if (values == null) continue;

      for (int i = 0; i < emotions.size(); i++) {
        double value = values[i];
        if (token.negated()) value *= -1.0;

        scores.merge(emotions.get(i), value, Double::sum);
      }
    }

    return scores;
  }
}
