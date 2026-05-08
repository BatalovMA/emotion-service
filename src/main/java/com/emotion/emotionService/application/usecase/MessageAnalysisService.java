package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.service.AggregationService;
import com.emotion.emotionService.infrastructure.lexicon.HybridEmotionAnalyzer;
import com.emotion.emotionService.mapper.AnalysisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageAnalysisService {

  private final HybridEmotionAnalyzer hybridEmotionAnalyzer;
  private final AggregationService aggregationService;
  private final AnalysisMapper analysisMapper;

  public MessageAnalysis analyze(Message message) {
    EmotionResult result = hybridEmotionAnalyzer.analyze(message);
    double temperature =
        aggregationService.calculateTemperature(result.getSentiment(), result.getIntensity());
    return analysisMapper.toDomain(result, temperature);
  }
}
