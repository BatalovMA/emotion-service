package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EmotionFusionPolicy {

  private static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;
  private static final double MEDIUM_CONFIDENCE_THRESHOLD = 0.45;
  private static final double HIGH_CONFIDENCE_LEX_WEIGHT = 0.10;
  private static final double MEDIUM_CONFIDENCE_LEX_WEIGHT = 0.15;
  private static final double LOW_CONFIDENCE_LEX_WEIGHT = 0.25;
  private static final double SHORT_MESSAGE_LEX_WEIGHT = 0.30;
  private static final int SHORT_MESSAGE_WORD_LIMIT = 3;
  private static final int EMOTIONS_LIMIT = 3;

  public EmotionResult fuse(Message message, EmotionResult ml, LexiconResult lexicon) {
    int wordCount = message == null ? 0 : LexiconPreprocessor.countWords(message.text());
    boolean shortMessage = wordCount <= SHORT_MESSAGE_WORD_LIMIT;

    double lexWeight = resolveLexiconWeight(ml.getConfidence(), shortMessage);
    double mlWeight = 1.0 - lexWeight;

    double finalSentiment = mlWeight * ml.getSentiment() + lexWeight * lexicon.getSentiment();
    double finalIntensity = mlWeight * ml.getIntensity() + lexWeight * lexicon.getIntensity();

    List<String> fusedEmotions = resolveEmotions(ml, lexicon, shortMessage);

    return EmotionResult.builder()
        .speaker(message == null ? null : message.speaker())
        .sentiment(finalSentiment)
        .emotion(fusedEmotions)
        .intensity(finalIntensity)
        .confidence(ml.getConfidence())
        .build();
  }

  private double resolveLexiconWeight(double confidence, boolean shortMessage) {
    double lexWeight;
    if (confidence >= HIGH_CONFIDENCE_THRESHOLD) {
      lexWeight = HIGH_CONFIDENCE_LEX_WEIGHT;
    } else if (confidence >= MEDIUM_CONFIDENCE_THRESHOLD) {
      lexWeight = MEDIUM_CONFIDENCE_LEX_WEIGHT;
    } else {
      lexWeight = LOW_CONFIDENCE_LEX_WEIGHT;
    }
    if (shortMessage) {
      lexWeight = SHORT_MESSAGE_LEX_WEIGHT;
    }
    return Math.clamp(lexWeight, 0.0, 0.5);
  }

  private List<String> resolveEmotions(
      EmotionResult ml, LexiconResult lexicon, boolean shortMessage) {
    List<String> mlEmotions = safeList(ml.getEmotion());
    List<String> lexEmotions = safeList(lexicon.getEmotions());

    if (mlEmotions.isEmpty()) {
      return limit(lexEmotions);
    }

    if (ml.getConfidence() >= HIGH_CONFIDENCE_THRESHOLD || lexEmotions.isEmpty()) {
      return limit(mlEmotions);
    }

    List<String> boosted = boostEmotions(mlEmotions, lexEmotions);
    if (boosted.isEmpty() && shortMessage && ml.getConfidence() < MEDIUM_CONFIDENCE_THRESHOLD) {
      return limit(lexEmotions);
    }
    return limit(boosted.isEmpty() ? mlEmotions : boosted);
  }

  private List<String> boostEmotions(List<String> mlEmotions, List<String> lexEmotions) {
    Set<String> mlSet = new LinkedHashSet<>(mlEmotions);
    LinkedHashSet<String> boosted = new LinkedHashSet<>();
    for (String lexEmotion : lexEmotions) {
      if (mlSet.contains(lexEmotion)) {
        boosted.add(lexEmotion);
      }
    }
    boosted.addAll(mlEmotions);
    return new ArrayList<>(boosted);
  }

  private List<String> safeList(List<String> emotions) {
    return emotions == null ? List.of() : emotions;
  }

  private List<String> limit(List<String> emotions) {
    if (emotions == null || emotions.isEmpty()) {
      return List.of("neutral");
    }
    return emotions.stream().limit(EMOTIONS_LIMIT).toList();
  }
}
