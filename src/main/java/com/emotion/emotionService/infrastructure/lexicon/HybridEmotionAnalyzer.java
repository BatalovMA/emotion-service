package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HybridEmotionAnalyzer {

  private final EmotionInferenceEngine inferenceEngine;
  private final LexiconAnalyzer lexiconAnalyzer;
  private final EmotionFusionPolicy fusionPolicy;

  public EmotionResult analyze(Message message) {

    EmotionResult ml = inferenceEngine.analyze(message);
    LexiconResult lex = lexiconAnalyzer.analyze(message.text());

    return fusionPolicy.fuse(message, ml, lex);
  }
}
