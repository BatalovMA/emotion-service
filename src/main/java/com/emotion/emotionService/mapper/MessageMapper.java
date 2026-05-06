package com.emotion.emotionService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.emotion.emotionService.api.dto.*;
import com.emotion.emotionService.domain.model.*;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  Message toDomain(MessageRequestDto dto);

  @Mapping(target = "speaker", source = "result.speaker")
  @Mapping(target = "temperature", source = "temperature")
  @Mapping(target = "emotion", source = "result.emotion")
  @Mapping(target = "confidence", source = "result.confidence")
  MessageAnalysisDto toDto(EmotionResult result, double temperature);
}
