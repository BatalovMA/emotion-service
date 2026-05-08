package com.emotion.emotionService.api.dto;

import java.util.List;

public record DialogueResponseDto(
    Double overallTemperature,
    String dominantDialogueEmotion,
    List<ParticipantAnalysisDto> participants,
    List<MessageAnalysisDto> messages,
    TrajectoryDto trajectory) {}
