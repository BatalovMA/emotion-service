package com.emotion.emotionService.api.controller;

import com.emotion.emotionService.api.dto.ContextMessageAnalysisResponseDto;
import com.emotion.emotionService.api.dto.ContextMessageRequestDto;
import com.emotion.emotionService.api.dto.DialogueRequestDto;
import com.emotion.emotionService.api.dto.DialogueResponseDto;
import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.api.dto.MessageRequestDto;
import com.emotion.emotionService.application.usecase.AnalyzeDialogueUseCase;
import com.emotion.emotionService.application.usecase.AnalyzeMessageUseCase;
import com.emotion.emotionService.application.usecase.AnalyzeMessageWithContextUseCase;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.mapper.AnalysisMapper;
import com.emotion.emotionService.mapper.MessageMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emotion")
@RequiredArgsConstructor
public class EmotionController {

  private final AnalyzeMessageUseCase analyzeMessageUseCase;
  private final AnalyzeDialogueUseCase analyzeDialogueUseCase;
  private final AnalyzeMessageWithContextUseCase analyzeMessageWithContextUseCase;
  private final MessageMapper mapper;
  private final AnalysisMapper analysisMapper;

  @PostMapping("/message")
  public MessageAnalysisDto analyzeMessage(@Valid @RequestBody MessageRequestDto request) {
    Message message = mapper.toDomain(request);
    return analyzeMessageUseCase.execute(message);
  }

  @PostMapping("/dialogue")
  public DialogueResponseDto analyzeDialogue(@Valid @RequestBody DialogueRequestDto request) {
    List<MessageRequestDto> requestMessages =
        request.messages() == null ? List.of() : request.messages();
    List<Message> messages = mapper.toDomain(requestMessages);
    return analysisMapper.toDto(analyzeDialogueUseCase.execute(messages));
  }

  @PostMapping("/message/with-context")
  public ContextMessageAnalysisResponseDto analyzeMessageWithContext(
      @Valid @RequestBody ContextMessageRequestDto request) {
    Message message = mapper.toDomain(request);
    return analysisMapper.toDto(
        analyzeMessageWithContextUseCase.execute(request.sessionId(), message));
  }

  @GetMapping("/message/with-context/session/{sessionId}")
  public List<Message> getSessionMessages(
          @PathVariable UUID sessionId) {
    return analyzeMessageWithContextUseCase.getSessionMessages(sessionId);
  }
}
