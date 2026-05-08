package com.emotion.emotionService.api.dto;

import java.util.UUID;

public record ContextMessageAnalysisResponseDto(
    UUID sessionId,
    MessageAnalysisDto message,
    Double overallTemperature,
    String dominantDialogueEmotion,
    TrajectoryDto trajectory
) {}

