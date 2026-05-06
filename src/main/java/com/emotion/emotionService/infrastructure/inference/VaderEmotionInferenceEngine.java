package com.emotion.emotionService.infrastructure.inference;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;

import com.emotion.emotionService.infrastructure.lexicon.VaderLikeAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class VaderEmotionInferenceEngine implements EmotionInferenceEngine {

  private final VaderLikeAnalyzer analyzer;

  // TODO: replace mapping with ML-backed inference
  @Override
  public EmotionResult analyze(Message message) {

    double compound = analyzer.sentiment(message.getText());

    return EmotionResult.builder()
        .speaker(message.getSpeaker())
        .sentiment(compound)
        .emotion(map(compound))
        .intensity(analyzer.intensity(message.getText()))
        .confidence(0.5 + Math.abs(compound) * 0.5)
        .build();
  }

  private List<String> map(double c) {
    if (c >= 0.05) return List.of("positive");
    if (c <= -0.05) return List.of("negative");
    return List.of("neutral");
  }
}
