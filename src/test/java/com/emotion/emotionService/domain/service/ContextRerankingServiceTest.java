package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextRerankingServiceTest {

  private final ContextRerankingService service = new ContextRerankingService();

  @Test
  void boostsDominantEmotionForShortMessages() {
    List<MessageAnalysis> previous = List.of(
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.6)
            .emotion(List.of("anger"))
            .confidence(0.8)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.5)
            .emotion(List.of("anger"))
            .confidence(0.7)
            .build());

    MessageAnalysis current = MessageAnalysis.builder()
        .speaker("user")
        .temperature(-0.1)
        .emotion(List.of("neutral"))
        .confidence(0.5)
        .build();

    MessageAnalysis reranked = service.rerank(current, previous, "fine");

    assertEquals("anger", reranked.getEmotion().getFirst());
    assertTrue(reranked.getConfidence() > current.getConfidence());
  }

  @Test
  void penalizesContradictionWhenContextIsPositive() {
    List<MessageAnalysis> previous = List.of(
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(0.6)
            .emotion(List.of("joy"))
            .confidence(0.9)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(0.4)
            .emotion(List.of("joy"))
            .confidence(0.9)
            .build());

    MessageAnalysis current = MessageAnalysis.builder()
        .speaker("user")
        .temperature(-0.5)
        .emotion(List.of("anger"))
        .confidence(0.6)
        .build();

    MessageAnalysis reranked = service.rerank(current, previous, "not good");

    assertTrue(reranked.getConfidence() < current.getConfidence());
  }
}

