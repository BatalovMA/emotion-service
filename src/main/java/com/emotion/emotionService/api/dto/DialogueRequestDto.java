package com.emotion.emotionService.api.dto;

import java.util.List;

public record DialogueRequestDto(List<MessageRequestDto> messages) {}
