package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.service.AggregationService;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeMessageUseCase {

    private final EmotionInferenceEngine inferenceEngine;
    private final AggregationService aggregationService;

    public MessageAnalysisDto execute(Message message) {
        EmotionResult result = inferenceEngine.analyze(message);

        double temperature = aggregationService
                .calculateTemperature(result.getSentiment(), result.getIntensity());

        return MessageAnalysisDto.builder()
                .speaker(result.getSpeaker())
                .temperature(temperature)
                .sentiment(result.getSentiment())
                .emotion(result.getEmotion())
                .intensity(result.getIntensity())
                .confidence(result.getConfidence())
                .build();
    }
}
