package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.config.ContextProperties;
import com.emotion.emotionService.domain.model.ContextMessageAnalysis;
import com.emotion.emotionService.domain.model.DialogueAnalysis;
import com.emotion.emotionService.domain.model.DialogueContext;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.Trajectory;
import com.emotion.emotionService.domain.service.ContextCacheRepository;
import com.emotion.emotionService.domain.service.ContextRerankingService;
import com.emotion.emotionService.domain.service.DialogueAggregationService;
import com.emotion.emotionService.domain.service.TrajectoryAnalysisService;
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
  private final ContextRerankingService contextRerankingService;
  private final DialogueAggregationService dialogueAggregationService;
  private final TrajectoryAnalysisService trajectoryAnalysisService;

  public ContextMessageAnalysis execute(UUID sessionId, Message message) {
    UUID resolvedSessionId = resolveSessionId(sessionId);
    List<Message> contextMessages = loadContextMessages(resolvedSessionId);

    List<Message> updatedMessages = new ArrayList<>(contextMessages);
    updatedMessages.add(message);
    updatedMessages = trimToWindow(updatedMessages, contextProperties.windowSize());

    DialogueAnalysis dialogueAnalysis = analyzeDialogueUseCase.execute(updatedMessages);
    List<MessageAnalysis> rerankedMessages = rerankLatest(dialogueAnalysis.getMessages(), message);
    MessageAnalysis latestMessageAnalysis = latestMessage(rerankedMessages);

    double overallTemperature = dialogueAggregationService.calculateOverallTemperature(rerankedMessages);
    String dominantDialogueEmotion = dialogueAggregationService.findDominantDialogueEmotion(rerankedMessages);
    Trajectory trajectory = trajectoryAnalysisService.analyze(rerankedMessages);

    DialogueContext updatedContext = DialogueContext.builder()
        .sessionId(resolvedSessionId)
        .messages(updatedMessages)
        .updatedAt(Instant.now())
        .build();
    contextCacheRepository.save(updatedContext);

    return ContextMessageAnalysis.builder()
        .sessionId(resolvedSessionId)
        .message(latestMessageAnalysis)
        .overallTemperature(overallTemperature)
        .dominantDialogueEmotion(dominantDialogueEmotion)
        .trajectory(trajectory)
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
        .map(DialogueContext::messages)
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

  private List<MessageAnalysis> rerankLatest(List<MessageAnalysis> messages, Message message) {
    if (messages == null || messages.isEmpty()) {
      return List.of();
    }
    if (messages.size() == 1) {
      MessageAnalysis reranked =
          contextRerankingService.rerank(messages.getFirst(), List.of(), messageText(message));
      return List.of(reranked == null ? messages.getFirst() : reranked);
    }

    List<MessageAnalysis> updated = new ArrayList<>(messages);
    MessageAnalysis latest = messages.getLast();
    List<MessageAnalysis> previous = messages.subList(0, messages.size() - 1);
    MessageAnalysis reranked =
        contextRerankingService.rerank(latest, previous, messageText(message));
    if (reranked != null) {
      updated.set(updated.size() - 1, reranked);
    }
    return updated;
  }

  private String messageText(Message message) {
    return message == null ? null : message.text();
  }
}
