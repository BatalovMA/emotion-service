package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.Trajectory;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrajectoryAnalysisServiceTest {

  private final TrajectoryAnalysisService service = new TrajectoryAnalysisService();

  @Test
  void analyzesTrajectoryForNegativeTrend() {
    List<MessageAnalysis> messages = List.of(
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.2)
            .emotion(List.of("sadness"))
            .confidence(0.7)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.5)
            .emotion(List.of("sadness"))
            .confidence(0.8)
            .build(),
        MessageAnalysis.builder()
            .speaker("user")
            .temperature(-0.8)
            .emotion(List.of("sadness"))
            .confidence(0.9)
            .build());

    Trajectory trajectory = service.analyze(messages);

    assertEquals(-0.2, trajectory.getStartTemperature());
    assertEquals(-0.8, trajectory.getEndTemperature());
    assertEquals(0.30000000000000004, trajectory.getVolatility());
    assertEquals("negative", trajectory.getTrend());
  }
}
