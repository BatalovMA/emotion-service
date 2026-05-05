package com.emotion.emotionService.infrastructure.inference;

import com.emotion.emotionService.domain.model.*;

public interface EmotionInferenceEngine {
    EmotionResult analyze(Message message);
}
