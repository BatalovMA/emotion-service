package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HybridEmotionAnalyzer {

  private final EmotionInferenceEngine inferenceEngine;
  private final LexiconAnalyzer lexiconAnalyzer;

  public EmotionResult analyze(Message message) {

    EmotionResult ml = inferenceEngine.analyze(message);
    LexiconResult lex = lexiconAnalyzer.analyze(message.getText());

    double finalSentiment = 0.85 * ml.getSentiment() + 0.15 * lex.getSentiment();
    double finalIntensity = 0.85 * ml.getIntensity() + 0.15 * lex.getIntensity();

    List<String> emotions = ml.getConfidence() > 0.6 ? ml.getEmotion() : lex.getEmotions();

    return EmotionResult.builder()
        .speaker(message.getSpeaker())
        .sentiment(finalSentiment)
        .emotion(emotions)
        .intensity(finalIntensity)
        .confidence(ml.getConfidence())
        .build();
  }
}
