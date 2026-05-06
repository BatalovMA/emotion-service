package com.emotion.emotionService.domain.model;

import java.util.List;

@lombok.Value
@lombok.Builder
public class LexiconResult {
  double sentiment;
  double intensity;
  List<String> emotions;
}
