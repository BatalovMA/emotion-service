package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HybridEmotionAnalyzer {

  private final EmotionInferenceEngine inferenceEngine;
  private final LexiconAnalyzer lexiconAnalyzer;

  public EmotionResult analyze(Message message) {

    EmotionResult ml = inferenceEngine.analyze(message);
    LexiconResult lex = lexiconAnalyzer.analyze(message.text());

    double finalSentiment = 0.85 * ml.getSentiment() + 0.15 * lex.getSentiment();
    double finalIntensity = 0.85 * ml.getIntensity() + 0.15 * lex.getIntensity();

    List<String> emotions = ml.getConfidence() > 0.5 ? ml.getEmotion() : lex.getEmotions();
    List<String> topEmotions = emotions.stream().limit(3).collect(Collectors.toList());

    return EmotionResult.builder()
        .speaker(message.speaker())
        .sentiment(finalSentiment)
        .emotion(topEmotions)
        .intensity(finalIntensity)
        .confidence(ml.getConfidence())
        .build();
  }
}
