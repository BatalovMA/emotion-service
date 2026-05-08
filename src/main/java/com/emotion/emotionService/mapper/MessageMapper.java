package com.emotion.emotionService.mapper;

import com.emotion.emotionService.api.dto.*;
import com.emotion.emotionService.domain.model.*;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  Message toDomain(MessageRequestDto dto);

  List<Message> toDomain(List<MessageRequestDto> dto);
}
