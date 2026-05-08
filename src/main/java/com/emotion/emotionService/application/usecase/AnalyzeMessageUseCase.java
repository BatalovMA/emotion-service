package com.emotion.emotionService.application.usecase;

import com.emotion.emotionService.api.dto.MessageAnalysisDto;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.domain.model.MessageAnalysis;
import com.emotion.emotionService.mapper.AnalysisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyzeMessageUseCase {

  private final MessageAnalysisService messageAnalysisService;
  private final AnalysisMapper analysisMapper;

  public MessageAnalysisDto execute(Message message) {
    MessageAnalysis analysis = messageAnalysisService.analyze(message);
    return analysisMapper.toDto(analysis);
  }
}
