package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.domain.model.DialogueAnalysis;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.ParticipantAnalysis;
import com.emotion.emotionService.domain.model.Trajectory;
import com.emotion.emotionService.domain.service.ContextRerankingService;
import com.emotion.emotionService.domain.service.DialogueAggregationService;
import com.emotion.emotionService.domain.service.ParticipantAggregationService;
import com.emotion.emotionService.domain.service.TrajectoryAnalysisService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeDialogueUseCase {

  private final MessageAnalysisService messageAnalysisService;
  private final ContextRerankingService contextRerankingService;
  private final DialogueAggregationService dialogueAggregationService;
  private final ParticipantAggregationService participantAggregationService;
  private final TrajectoryAnalysisService trajectoryAnalysisService;

  public DialogueAnalysis execute(List<Message> messages) {
    List<MessageAnalysis> messageAnalyses = buildContextAwareAnalyses(messages);

    double overallTemperature =
        dialogueAggregationService.calculateOverallTemperature(messageAnalyses);
    String dominantDialogueEmotion =
        dialogueAggregationService.findDominantDialogueEmotion(messageAnalyses);
    List<ParticipantAnalysis> participants =
        participantAggregationService.aggregate(messageAnalyses);
    Trajectory trajectory = trajectoryAnalysisService.analyze(messageAnalyses);

    return DialogueAnalysis.builder()
        .overallTemperature(overallTemperature)
        .dominantDialogueEmotion(dominantDialogueEmotion)
        .participants(participants)
        .messages(messageAnalyses)
        .trajectory(trajectory)
        .build();
  }

  private List<MessageAnalysis> buildContextAwareAnalyses(List<Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return List.of();
    }

    List<MessageAnalysis> analyses = new ArrayList<>();
    messages
        .forEach(
            message -> {
              MessageAnalysis analysis = messageAnalysisService.analyze(message);
              MessageAnalysis reranked =
                  contextRerankingService.rerank(analysis, analyses, messageText(message));
              analyses.add(reranked == null ? analysis : reranked);
            });
    return analyses;
  }

  private String messageText(Message message) {
    return message == null ? null : message.text();
  }
}
