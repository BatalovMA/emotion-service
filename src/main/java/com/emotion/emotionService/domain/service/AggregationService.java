package com.emotion.emotionService.domain.service;

import org.springframework.stereotype.Service;

@Service
public class AggregationService {

    public double calculateTemperature(double sentiment, double intensity) {
        return sentiment * intensity;
    }
}
