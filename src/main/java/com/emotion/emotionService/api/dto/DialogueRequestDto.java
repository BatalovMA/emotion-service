package com.emotion.emotionService.api.dto;

import java.util.List;

@lombok.Data
public class DialogueRequestDto {
  private String dialogId;
  private List<MessageRequestDto> messages;
}
