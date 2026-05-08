package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.MessageAnalysis;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DialogueAggregationService {

  public double calculateOverallTemperature(List<MessageAnalysis> messages) {
    return EmotionStatistics.averageTemperature(messages);
  }

  public String findDominantDialogueEmotion(List<MessageAnalysis> messages) {
    return EmotionStatistics.findDominantEmotion(messages, "neutral");
  }
}
