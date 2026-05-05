package com.emotion.emotionService.api.controller;

import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.api.dto.MessageRequestDto;
import com.emotion.emotionService.application.usecase.AnalyzeMessageUseCase;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.mapper.MessageMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emotion")
@RequiredArgsConstructor
public class EmotionController {

    private final AnalyzeMessageUseCase analyzeMessageUseCase;
    private final MessageMapper mapper;

    @PostMapping("/message")
    public MessageAnalysisDto analyzeMessage(
            @Valid @RequestBody MessageRequestDto request
    ) {
        Message message = mapper.toDomain(request);
        return analyzeMessageUseCase.execute(message);
    }
}
