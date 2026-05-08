package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.ParticipantAnalysis;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipantAggregationServiceTest {

  private final ParticipantAggregationService service = new ParticipantAggregationService();

  @Test
  void aggregatesParticipantsWithTrendAndDominantEmotion() {
    List<MessageAnalysis> messages = List.of(
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.2)
            .emotion(List.of("sadness"))
            .confidence(0.7)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.6)
            .emotion(List.of("anger"))
            .confidence(0.8)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.9)
            .emotion(List.of("anger"))
            .confidence(0.9)
            .build(),
        MessageAnalysis.builder()
            .speaker("bot")
            .temperature(0.1)
            .emotion(List.of("neutral"))
            .confidence(0.6)
            .build());

    List<ParticipantAnalysis> participants = service.aggregate(messages);

    ParticipantAnalysis user = participants.get(0);
    assertEquals("user", user.getSpeaker());
    assertEquals("anger", user.getDominantEmotion());
    assertEquals("escalating", user.getEmotionalTrend());
  }
}
