package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.service.AggregationService;
import com.emotion.emotionService.infrastructure.lexicon.HybridEmotionAnalyzer;
import com.emotion.emotionService.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeMessageUseCase {

    private final HybridEmotionAnalyzer hybridEmotionAnalyzer;
    private final AggregationService aggregationService;
    private final MessageMapper mapper;

    public MessageAnalysisDto execute(Message message) {
        EmotionResult result = hybridEmotionAnalyzer.analyze(message);

        double temperature = aggregationService
                .calculateTemperature(result.getSentiment(), result.getIntensity());

        return mapper.toDto(result, temperature);
    }
}
