package com.emotion.emotionService.infrastructure.lexicon;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepecheMoodAnalyzerTest {

  private DepecheMoodAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new DepecheMoodAnalyzer();
    analyzer.init();
  }

  @Test
  void analyzeScoresReturnsExpectedValues() {
    Map<String, Double> scores = analyzer.analyzeScores("aa");

    assertTrue(scores.containsKey("inspired"));
    assertTrue(scores.get("inspired") > 0.0);
  }

  @Test
  void analyzeScoresHandlesNegation() {
    Map<String, Double> scores = analyzer.analyzeScores("not aa");

    assertNotNull(scores.get("inspired"));
    assertTrue(scores.get("inspired") < 0.0);
  }
}
