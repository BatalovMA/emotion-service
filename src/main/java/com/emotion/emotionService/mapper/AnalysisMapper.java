package com.emotion.emotionService.mapper;

import com.emotion.emotionService.api.dto.ContextMessageAnalysisResponseDto;
import com.emotion.emotionService.api.dto.DialogueResponseDto;
import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.api.dto.ParticipantAnalysisDto;
import com.emotion.emotionService.api.dto.TrajectoryDto;
import com.emotion.emotionService.domain.model.ContextMessageAnalysis;
import com.emotion.emotionService.domain.model.DialogueAnalysis;
import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.domain.model.ParticipantAnalysis;
import com.emotion.emotionService.domain.model.Trajectory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

  @Mapping(target = "speaker", source = "result.speaker")
  @Mapping(target = "temperature", source = "temperature")
  @Mapping(target = "emotion", source = "result.emotion")
  @Mapping(target = "confidence", source = "result.confidence")
  MessageAnalysis toDomain(EmotionResult result, double temperature);

  MessageAnalysisDto toDto(MessageAnalysis analysis);

  ParticipantAnalysisDto toDto(ParticipantAnalysis analysis);

  TrajectoryDto toDto(Trajectory trajectory);

  DialogueResponseDto toDto(DialogueAnalysis analysis);

  ContextMessageAnalysisResponseDto toDto(ContextMessageAnalysis analysis);
}
