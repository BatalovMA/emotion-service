package com.emotion.emotionService.mapper;

import com.emotion.emotionService.api.dto.*;
import com.emotion.emotionService.domain.model.*;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
  Message toDomain(MessageRequestDto dto);

  @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
  Message toDomain(ContextMessageRequestDto dto);

  List<Message> toDomain(List<MessageRequestDto> dto);
}
