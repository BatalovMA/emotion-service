package com.emotion.emotionService.domain.model;

import java.util.List;

@lombok.Value
@lombok.Builder
public class DialogueAnalysis {
  double overallTemperature;
  String dominantDialogueEmotion;
  List<ParticipantAnalysis> participants;
  List<MessageAnalysis> messages;
  Trajectory trajectory;
}
