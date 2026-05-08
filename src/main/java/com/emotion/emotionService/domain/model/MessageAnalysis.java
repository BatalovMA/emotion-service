package com.emotion.emotionService.domain.model;

import java.util.List;

@lombok.Value
@lombok.Builder
public class MessageAnalysis {
  String speaker;
  double temperature;
  List<String> emotion;
  double confidence;
}
