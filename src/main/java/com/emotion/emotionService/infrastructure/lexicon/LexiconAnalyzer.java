package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.LexiconResult;

public interface LexiconAnalyzer {
  LexiconResult analyze(String text);
}
