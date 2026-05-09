package com.emotion.emotionService.infrastructure.lexicon;

import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HybridEmotionAnalyzer {

  private static final double DEFAULT_ML_WEIGHT = 0.85;
  private static final double DEFAULT_LEXICON_WEIGHT = 0.15;
  private static final double SHORT_MESSAGE_ML_WEIGHT = 0.70;
  private static final double SHORT_MESSAGE_LEXICON_WEIGHT = 0.30;
  private static final int SHORT_MESSAGE_WORD_LIMIT = 3;
  private static final int EMOTIONS_LIMIT = 3;

  private final EmotionInferenceEngine inferenceEngine;
  private final LexiconAnalyzer lexiconAnalyzer;

  public EmotionResult analyze(Message message) {

    EmotionResult ml = inferenceEngine.analyze(message);
    LexiconResult lex = lexiconAnalyzer.analyze(message.text());

    boolean shortMessage =
        LexiconPreprocessor.countWords(message.text()) <= SHORT_MESSAGE_WORD_LIMIT;
    double mlWeight = shortMessage ? SHORT_MESSAGE_ML_WEIGHT : DEFAULT_ML_WEIGHT;
    double lexWeight = shortMessage ? SHORT_MESSAGE_LEXICON_WEIGHT : DEFAULT_LEXICON_WEIGHT;

    double finalSentiment = mlWeight * ml.getSentiment() + lexWeight * lex.getSentiment();
    double finalIntensity = mlWeight * ml.getIntensity() + lexWeight * lex.getIntensity();

    List<String> emotions = ml.getConfidence() > 0.5 ? ml.getEmotion() : lex.getEmotions();
    List<String> topEmotions = emotions.stream().limit(EMOTIONS_LIMIT).collect(Collectors.toList());

    return EmotionResult.builder()
        .speaker(message.speaker())
        .sentiment(finalSentiment)
        .emotion(topEmotions)
        .intensity(finalIntensity)
        .confidence(ml.getConfidence())
        .build();
  }
}
