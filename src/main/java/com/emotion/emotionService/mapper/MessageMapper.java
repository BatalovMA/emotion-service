package com.emotion.emotionService.mapper;

import org.mapstruct.Mapper;
import com.emotion.emotionService.api.dto.*;
import com.emotion.emotionService.domain.model.*;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  Message toDomain(MessageRequestDto dto);

  MessageAnalysisDto toDto(EmotionResult result);
}
