package com.emotion.emotionService.api.dto;

@lombok.Data
public class ParticipantAnalysisDto {
  private String speaker;
  private Double temperature;
  private String dominantEmotion;
}
