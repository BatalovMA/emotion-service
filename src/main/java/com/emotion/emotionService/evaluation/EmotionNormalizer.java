package com.emotion.emotionService.evaluation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EmotionNormalizer {

  private final Map<String, String> mapping = new HashMap<>();

  public EmotionNormalizer() {
    mapping.put("anger", "anger");
    mapping.put("annoyance", "anger");
    mapping.put("disapproval", "anger");
    mapping.put("angry", "anger");
    mapping.put("annoyed", "anger");

    mapping.put("sadness", "sadness");
    mapping.put("grief", "sadness");
    mapping.put("disappointment", "sadness");
    mapping.put("remorse", "sadness");
    mapping.put("embarrassment", "sadness");
    mapping.put("sad", "sadness");

    mapping.put("fear", "fear");
    mapping.put("nervousness", "fear");
    mapping.put("afraid", "fear");

    mapping.put("disgust", "disgust");

    mapping.put("surprise", "surprise");

    mapping.put("joy", "joy");
    mapping.put("amusement", "joy");
    mapping.put("excitement", "joy");
    mapping.put("love", "joy");
    mapping.put("happy", "joy");
    mapping.put("amused", "joy");

    mapping.put("admiration", "trust");
    mapping.put("approval", "trust");
    mapping.put("gratitude", "trust");
    mapping.put("caring", "trust");
    mapping.put("pride", "trust");
    mapping.put("relief", "trust");

    mapping.put("optimism", "anticipation");
    mapping.put("desire", "anticipation");
    mapping.put("curiosity", "anticipation");

    mapping.put("confusion", "neutral");
    mapping.put("realization", "neutral");
    mapping.put("dont_care", "neutral");
    mapping.put("neutral", "neutral");

    mapping.put("positive", "positive");
    mapping.put("negative", "negative");
  }

  public String normalize(String rawEmotion) {
    if (rawEmotion == null || rawEmotion.isBlank()) {
      return "neutral";
    }
    String key = rawEmotion.toLowerCase(Locale.ROOT).trim();
    return mapping.getOrDefault(key, "neutral");
  }

  public Set<String> normalizeAll(Set<String> rawEmotions) {
    if (rawEmotions == null || rawEmotions.isEmpty()) {
      return Set.of("neutral");
    }
    return rawEmotions.stream().map(this::normalize).collect(Collectors.toSet());
  }
}
