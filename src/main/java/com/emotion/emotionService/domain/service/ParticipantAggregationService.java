package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.ParticipantAnalysis;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ParticipantAggregationService {

  private static final double VOLATILITY_THRESHOLD = 0.6;
  private static final double TREND_THRESHOLD = 0.2;

  public List<ParticipantAnalysis> aggregate(List<MessageAnalysis> messages) {
    if (messages == null || messages.isEmpty()) {
      return List.of();
    }

    Map<String, List<MessageAnalysis>> bySpeaker =
        messages.stream()
            .collect(
                Collectors.groupingBy(
                    MessageAnalysis::getSpeaker, LinkedHashMap::new, Collectors.toList()));

    List<ParticipantAnalysis> participants = new ArrayList<>();
    for (Map.Entry<String, List<MessageAnalysis>> entry : bySpeaker.entrySet()) {
      List<MessageAnalysis> speakerMessages = entry.getValue();
      double averageTemperature =
          speakerMessages.stream()
              .mapToDouble(MessageAnalysis::getTemperature)
              .average()
              .orElse(0.0);

      String dominantEmotion = EmotionStatistics.findDominantEmotion(speakerMessages, "neutral");
      String emotionalTrend = analyzeTrend(speakerMessages);

      participants.add(
          ParticipantAnalysis.builder()
              .speaker(entry.getKey())
              .temperature(averageTemperature)
              .dominantEmotion(dominantEmotion)
              .emotionalTrend(emotionalTrend)
              .build());
    }

    return participants;
  }

  private String analyzeTrend(List<MessageAnalysis> messages) {
    if (messages.size() < 2) {
      return "stable";
    }

    List<Double> temperatures = messages.stream().map(MessageAnalysis::getTemperature).toList();

    double volatility = EmotionStatistics.averageAbsoluteDelta(temperatures);
    if (volatility >= VOLATILITY_THRESHOLD) {
      return "volatile";
    }

    double startAbs = Math.abs(temperatures.get(0));
    double endAbs = Math.abs(temperatures.get(temperatures.size() - 1));

    if (endAbs - startAbs > TREND_THRESHOLD) {
      return "escalating";
    }
    if (startAbs - endAbs > TREND_THRESHOLD) {
      return "decreasing";
    }

    return "stable";
  }
}
