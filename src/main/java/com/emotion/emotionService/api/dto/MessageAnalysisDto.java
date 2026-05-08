package com.emotion.emotionService.api.dto;

import java.util.List;

public record MessageAnalysisDto(
    String speaker, Double temperature, List<String> emotion, Double confidence) {}
