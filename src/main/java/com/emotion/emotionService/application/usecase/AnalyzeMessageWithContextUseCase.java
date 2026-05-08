package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.config.ContextProperties;
import com.emotion.emotionService.domain.model.ContextMessageAnalysis;
import com.emotion.emotionService.domain.model.DialogueAnalysis;
import com.emotion.emotionService.domain.model.DialogueContext;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.service.ContextCacheRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeMessageWithContextUseCase {

  private final AnalyzeDialogueUseCase analyzeDialogueUseCase;
  private final ContextCacheRepository contextCacheRepository;
  private final ContextProperties contextProperties;

  public ContextMessageAnalysis execute(UUID sessionId, Message message) {
    UUID resolvedSessionId = resolveSessionId(sessionId);
    List<Message> contextMessages = loadContextMessages(resolvedSessionId);

    List<Message> updatedMessages = new ArrayList<>(contextMessages);
    updatedMessages.add(message);
    updatedMessages = trimToWindow(updatedMessages, contextProperties.windowSize());

    DialogueAnalysis dialogueAnalysis = analyzeDialogueUseCase.execute(updatedMessages);
    MessageAnalysis latestMessageAnalysis = latestMessage(dialogueAnalysis.getMessages());

    DialogueContext updatedContext = DialogueContext.builder()
        .sessionId(resolvedSessionId)
        .messages(updatedMessages)
        .updatedAt(Instant.now())
        .build();
    contextCacheRepository.save(updatedContext);

    return ContextMessageAnalysis.builder()
        .sessionId(resolvedSessionId)
        .message(latestMessageAnalysis)
        .overallTemperature(dialogueAnalysis.getOverallTemperature())
        .dominantDialogueEmotion(dialogueAnalysis.getDominantDialogueEmotion())
        .trajectory(dialogueAnalysis.getTrajectory())
        .build();
  }

  public List<Message> getSessionMessages(UUID resolvedSessionId) {
    return loadContextMessages(resolvedSessionId);
  }

  private UUID resolveSessionId(UUID sessionId) {
    return sessionId == null ? UUID.randomUUID() : sessionId;
  }

  private List<Message> loadContextMessages(UUID sessionId) {
    return contextCacheRepository.findBySessionId(sessionId)
        .map(DialogueContext::getMessages)
        .orElseGet(List::of);
  }

  private List<Message> trimToWindow(List<Message> messages, int windowSize) {
    if (windowSize <= 0 || messages.size() <= windowSize) {
      return messages;
    }
    return new ArrayList<>(messages.subList(messages.size() - windowSize, messages.size()));
  }

  private MessageAnalysis latestMessage(List<MessageAnalysis> messages) {
    if (messages == null || messages.isEmpty()) {
      return null;
    }
    return messages.getLast();
  }
}

