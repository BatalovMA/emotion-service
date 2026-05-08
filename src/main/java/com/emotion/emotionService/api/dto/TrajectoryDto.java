package com.emotion.emotionService.api.dto;

public record TrajectoryDto(
    Double startTemperature, Double endTemperature, Double volatility, String trend) {}
