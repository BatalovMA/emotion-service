package com.emotion.emotionService.api.dto;

public record ParticipantAnalysisDto(
    String speaker, Double temperature, String dominantEmotion, String emotionalTrend) {}
