package com.emotion.emotionService.domain.model;

@lombok.Value
@lombok.Builder
public class Trajectory {
  double startTemperature;
  double endTemperature;
  double volatility;
  String trend;
}
