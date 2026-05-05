package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import org.springframework.stereotype.Service;

@Service
public class StubInferenceEngine implements EmotionInferenceEngine {

  @Override
  public EmotionResult analyze(Message message) {
    return EmotionResult.builder()
        .speaker(message.getSpeaker())
        .sentiment(-0.5)
        .emotion("neutral")
        .intensity(0.5)
        .confidence(0.8)
        .build();
  }
}
