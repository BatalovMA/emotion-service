package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogueAggregationServiceTest {

  private final DialogueAggregationService service = new DialogueAggregationService();

  @Test
  void calculatesOverallTemperatureAndDominantEmotion() {
    List<MessageAnalysis> messages = List.of(
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.5)
            .emotion(List.of("anger"))
            .confidence(0.8)
            .build(),
        MessageAnalysis.builder()
            .speaker("bot")
            .temperature(0.5)
            .emotion(List.of("joy"))
            .confidence(0.9)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.2)
            .emotion(List.of("anger"))
            .confidence(0.7)
            .build());

    assertEquals(-0.06666666666666667, service.calculateOverallTemperature(messages), 1e-9);
    assertEquals("anger", service.findDominantDialogueEmotion(messages));
  }
}
