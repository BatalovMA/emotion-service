package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Primary
@RequiredArgsConstructor
public class CompositeLexiconAnalyzer implements LexiconAnalyzer {

  private final VaderLikeAnalyzer vader;
  private final NrcEmotionAnalyzer nrc;

  @Override
  public LexiconResult analyze(String text) {

    double sentiment = vader.sentiment(text);
    double intensity = vader.intensity(text);

    Map<String, Double> emotions = nrc.analyze(text);

    List<String> rankedEmotions =
        emotions.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();

    if (rankedEmotions.isEmpty()) {
      rankedEmotions = List.of(resolveFallbackEmotion(sentiment));
    }

    return LexiconResult.builder()
        .sentiment(sentiment)
        .intensity(intensity)
        .emotions(rankedEmotions)
        .build();
  }

  private String resolveFallbackEmotion(double sentiment) {

    if (sentiment >= 0.3) {
      return "joy";
    }

    if (sentiment <= -0.3) {
      return "anger";
    }

    return "neutral";
  }
}
