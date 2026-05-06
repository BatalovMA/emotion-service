package com.emotion.emotionService.api.dto;

import java.util.List;

@lombok.Data
@lombok.Builder
public class MessageAnalysisDto {
  private String speaker;
  private Double temperature;
  private List<String> emotion;
  private Double confidence;
}
