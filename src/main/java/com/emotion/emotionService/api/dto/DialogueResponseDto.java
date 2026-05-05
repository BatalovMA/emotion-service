package com.emotion.emotionService.api.dto;

import java.util.List;

@lombok.Data
public class DialogueResponseDto {
  private String dialogId;
  private Double overallTemperature;
  private List<ParticipantAnalysisDto> participants;
  private List<MessageAnalysisDto> messages;
}
