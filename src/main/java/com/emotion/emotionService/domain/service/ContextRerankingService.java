package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ContextRerankingService {

  private static final int CONTEXT_WINDOW = 5;
  private static final int SHORT_MESSAGE_WORD_LIMIT = 5;
  private static final int EMOTION_LIMIT = 3;
  private static final double SHORT_MESSAGE_BOOST = 0.15;
  private static final double DEFAULT_BOOST = 0.08;
  private static final double CONTRADICTION_PENALTY = 0.08;
  private static final double HIGH_CONFIDENCE_THRESHOLD = 0.85;
  private static final double CONTEXT_NEUTRAL_BAND = 0.15;
  private static final double STRONG_CONTEXT_THRESHOLD = 0.35;

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
          Map.entry("neutral", 0.0));

  public MessageAnalysis rerank(
      MessageAnalysis current, List<MessageAnalysis> previousMessages, String currentText) {
    if (current == null) {
      return null;
    }
    if (previousMessages == null || previousMessages.isEmpty()) {
      return current;
    }

    List<MessageAnalysis> window = tail(previousMessages);
    List<MessageAnalysis> sameSpeaker = filterBySpeaker(window, current.getSpeaker());
    List<MessageAnalysis> contextSource = sameSpeaker.isEmpty() ? window : sameSpeaker;

    List<String> emotions = safeList(current.getEmotion());
    double averageTemperature = EmotionStatistics.averageTemperature(contextSource);
    String dominant = resolveDominantEmotion(contextSource, averageTemperature);
    if (dominant.isBlank()) {
      return current;
    }
    boolean shortMessage = countWords(currentText) < SHORT_MESSAGE_WORD_LIMIT;
    boolean contextAligned =
        emotions.contains(dominant) || (shortMessage && !"neutral".equals(dominant));
    boolean contradiction = isContradiction(averageTemperature, emotions);
    boolean strongContext = Math.abs(averageTemperature) >= STRONG_CONTEXT_THRESHOLD;

    double boost = shortMessage ? SHORT_MESSAGE_BOOST : DEFAULT_BOOST;
    double adjustedConfidence = current.getConfidence();
    if (contradiction && current.getConfidence() < HIGH_CONFIDENCE_THRESHOLD) {
      adjustedConfidence -= CONTRADICTION_PENALTY;
    } else if (contextAligned) {
      adjustedConfidence += boost * (1.0 - current.getConfidence());
    }
    adjustedConfidence = clamp(adjustedConfidence);

    List<String> reranked =
        rerankEmotions(emotions, dominant, shortMessage, contradiction, strongContext);

    return MessageAnalysis.builder()
        .speaker(current.getSpeaker())
        .temperature(current.getTemperature())
        .emotion(reranked)
        .confidence(adjustedConfidence)
        .build();
  }

  private List<MessageAnalysis> tail(List<MessageAnalysis> messages) {
    if (messages == null || messages.isEmpty()) {
      return List.of();
    }
    if (messages.size() <= ContextRerankingService.CONTEXT_WINDOW) {
      return messages;
    }
    return messages.subList(
        messages.size() - ContextRerankingService.CONTEXT_WINDOW, messages.size());
  }

  private List<String> rerankEmotions(
      List<String> emotions,
      String dominant,
      boolean shortMessage,
      boolean contradiction,
      boolean strongContext) {
    if (emotions == null || emotions.isEmpty()) {
      return List.of("neutral");
    }

    List<String> result = new ArrayList<>();
    if (dominant != null
        && (emotions.contains(dominant)
            || (shortMessage
                && !"neutral".equals(dominant)
                && (!contradiction || strongContext)))) {
      result.add(dominant);
    }
    for (String emotion : emotions) {
      if (!result.contains(emotion)) {
        result.add(emotion);
      }
    }

    return result.stream().limit(EMOTION_LIMIT).toList();
  }

  private boolean isContradiction(double averageTemperature, List<String> emotions) {
    if (Math.abs(averageTemperature) < CONTEXT_NEUTRAL_BAND) {
      return false;
    }
    String primary = emotions == null || emotions.isEmpty() ? "neutral" : emotions.getFirst();
    double polarity = EMOTION_POLARITY.getOrDefault(primary, 0.0);
    return averageTemperature < 0.0 && polarity > 0.2
        || averageTemperature > 0.0 && polarity < -0.2;
  }

  private int countWords(String text) {
    if (text == null) {
      return 0;
    }
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      return 0;
    }
    return trimmed.split("\\s+").length;
  }

  private List<String> safeList(List<String> emotions) {
    return emotions == null ? List.of() : emotions;
  }

  private List<MessageAnalysis> filterBySpeaker(List<MessageAnalysis> messages, String speaker) {
    if (messages == null || messages.isEmpty() || speaker == null || speaker.isBlank()) {
      return List.of();
    }
    return messages.stream()
        .filter(message -> speaker.equalsIgnoreCase(message.getSpeaker()))
        .toList();
  }

  private String resolveDominantEmotion(List<MessageAnalysis> messages, double averageTemperature) {
    String dominant = EmotionStatistics.findDominantEmotion(messages, null);
    if (dominant == null || dominant.isBlank() || "neutral".equals(dominant)) {
      if (averageTemperature <= -CONTEXT_NEUTRAL_BAND) {
        return "sadness";
      }
      if (averageTemperature >= CONTEXT_NEUTRAL_BAND) {
        return "joy";
      }
      return "neutral";
    }
    return dominant;
  }

  private double clamp(double value) {
    return Math.clamp(value, 0.0, 1.0);
  }
}
