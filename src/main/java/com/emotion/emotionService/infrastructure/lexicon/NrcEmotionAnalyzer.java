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

      String line;

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\t");

        String word = parts[0];
        String emotion = parts[1];
        int association = Integer.parseInt(parts[2]);

        if (association == 1) {
          wordToEmotions.computeIfAbsent(word, k -> new HashSet<>()).add(emotion);
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to load NRC lexicon", e);
    }
  }

  public Map<String, Double> analyze(String text) {

    String[] tokens = normalize(text);

    Map<String, Long> counts =
        Arrays.stream(tokens)
            .map(wordToEmotions::get)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.groupingBy(emotion -> emotion, Collectors.counting()));

    long total = counts.values().stream().mapToLong(Long::longValue).sum();

    if (total == 0) {
      return Collections.emptyMap();
    }

    return counts.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / (double) total));
  }

  private String[] normalize(String text) {
    return text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
  }
}
