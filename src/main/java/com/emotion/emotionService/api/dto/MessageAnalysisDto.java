package com.emotion.emotionService.api.dto;

@lombok.Data
@lombok.Builder
public class MessageAnalysisDto {
  private String speaker;
  private Double temperature;
  private Double sentiment;
  private String emotion;
  private Double intensity;
  private Double confidence;
}
