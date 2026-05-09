package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.Trajectory;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrajectoryAnalysisService {

  private static final double VOLATILITY_THRESHOLD = 0.6;
  private static final double TREND_THRESHOLD = 0.2;

  public Trajectory analyze(List<MessageAnalysis> messages) {
    if (messages == null || messages.isEmpty()) {
      return Trajectory.builder()
          .startTemperature(0.0)
          .endTemperature(0.0)
          .volatility(0.0)
          .trend("stable")
          .build();
    }

    double start = messages.getFirst().getTemperature();
    double end = messages.getLast().getTemperature();
    double volatility = EmotionStatistics.averageAbsoluteDeltaMessages(messages);

    return Trajectory.builder()
        .startTemperature(start)
        .endTemperature(end)
        .volatility(volatility)
        .trend(determineTrend(start, end, volatility))
        .build();
  }

  private String determineTrend(double start, double end, double volatility) {
    if (volatility >= VOLATILITY_THRESHOLD) {
      return "volatile";
    }
    if (end - start > TREND_THRESHOLD) {
      return "positive";
    }
    if (end - start < -TREND_THRESHOLD) {
      return "negative";
    }
    return "stable";
  }
}
