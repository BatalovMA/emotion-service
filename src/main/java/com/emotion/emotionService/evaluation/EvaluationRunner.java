package com.emotion.emotionService.evaluation;

import com.emotion.emotionService.EmotionServiceApplication;
import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.LexiconResult;
import com.emotion.emotionService.domain.model.Message;
import com.emotion.emotionService.infrastructure.inference.EmotionInferenceEngine;
import com.emotion.emotionService.infrastructure.lexicon.EmotionFusionPolicy;
import com.emotion.emotionService.infrastructure.lexicon.CompositeLexiconAnalyzer;
import com.emotion.emotionService.infrastructure.lexicon.LexiconScoringService;
import com.emotion.emotionService.infrastructure.lexicon.NrcEmotionAnalyzer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

public final class EvaluationRunner {

  private EvaluationRunner() {}

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(EmotionServiceApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);

    try (ConfigurableApplicationContext context = application.run(args)) {
      Map<String, String> options = parseArgs(args);
      int limit = parseLimit(options.get("limit"));
      EvaluationMode mode = EvaluationMode.from(options.get("mode"));

      GoEmotionsDatasetLoader loader = context.getBean(GoEmotionsDatasetLoader.class);
      EmotionNormalizer normalizer = context.getBean(EmotionNormalizer.class);
      EmotionInferenceEngine inferenceEngine = context.getBean(EmotionInferenceEngine.class);
      EmotionFusionPolicy fusionPolicy = context.getBean(EmotionFusionPolicy.class);
      CompositeLexiconAnalyzer compositeLexiconAnalyzer =
          context.getBean(CompositeLexiconAnalyzer.class);
      NrcEmotionAnalyzer nrcEmotionAnalyzer = context.getBean(NrcEmotionAnalyzer.class);
      LexiconScoringService scoringService = context.getBean(LexiconScoringService.class);

      String resourcePath = "/evaluation/test.tsv";
      List<GoEmotionsDatasetLoader.DatasetRecord> records = loader.load(resourcePath);
      if (limit > 0 && limit < records.size()) {
        records = records.subList(0, limit);
      }

      List<EvaluationMode> modes = mode == null ? List.of(EvaluationMode.values()) : List.of(mode);

      for (EvaluationMode evaluationMode : modes) {
        evaluateAndPrint(
            records,
            evaluationMode,
            normalizer,
            inferenceEngine,
            fusionPolicy,
            compositeLexiconAnalyzer,
            nrcEmotionAnalyzer,
            scoringService);
      }
    }
  }

  private static void evaluateAndPrint(
      List<GoEmotionsDatasetLoader.DatasetRecord> records,
      EvaluationMode mode,
      EmotionNormalizer normalizer,
      EmotionInferenceEngine inferenceEngine,
      EmotionFusionPolicy fusionPolicy,
      CompositeLexiconAnalyzer compositeLexiconAnalyzer,
      NrcEmotionAnalyzer nrcEmotionAnalyzer,
      LexiconScoringService scoringService) {
    int correct = 0;

    for (GoEmotionsDatasetLoader.DatasetRecord record : records) {
      String predicted =
          predictDominantEmotion(
              record,
              mode,
              inferenceEngine,
              fusionPolicy,
              compositeLexiconAnalyzer,
              nrcEmotionAnalyzer,
              scoringService);
      String predictedNormalized = normalizer.normalize(predicted);
      Set<String> normalizedLabels = normalizer.normalizeAll(record.labels());
      if (normalizedLabels.contains(predictedNormalized)) {
        correct++;
      }
    }

    int samples = records.size();
    double accuracy = ratio(correct, samples);
    double f1 = accuracy;

    System.out.println("Mode: " + mode.displayName());
    System.out.println("Samples: " + samples);
    System.out.printf("Accuracy: %.3f%n", accuracy);
    System.out.printf("F1: %.3f%n", f1);
    System.out.println("---");
  }

  private static String predictDominantEmotion(
      GoEmotionsDatasetLoader.DatasetRecord record,
      EvaluationMode mode,
      EmotionInferenceEngine inferenceEngine,
      EmotionFusionPolicy fusionPolicy,
      CompositeLexiconAnalyzer compositeLexiconAnalyzer,
      NrcEmotionAnalyzer nrcEmotionAnalyzer,
      LexiconScoringService scoringService) {
    Message message = new Message("user", record.text());
    EmotionResult ml = inferenceEngine.analyze(message);
    if (mode == EvaluationMode.ONNX_ONLY) {
      return dominantEmotion(ml);
    }

    LexiconResult lexicon =
        mode == EvaluationMode.ONNX_NRC
            ? scoringService.buildResult(nrcEmotionAnalyzer.analyze(message.text()), false)
            : compositeLexiconAnalyzer.analyze(message.text());

    return dominantEmotion(fusionPolicy.fuse(message, ml, lexicon));
  }

  private static String dominantEmotion(EmotionResult result) {
    List<String> emotions = result.getEmotion();
    if (emotions == null || emotions.isEmpty()) {
      return "neutral";
    }
    return emotions.getFirst();
  }

  private static Map<String, String> parseArgs(String[] args) {
    Map<String, String> values = new HashMap<>();
    if (args == null) {
      return values;
    }
    for (String arg : args) {
      if (arg == null || !arg.startsWith("--") || !arg.contains("=")) {
        continue;
      }
      String[] parts = arg.substring(2).split("=", 2);
      if (parts.length == 2) {
        values.put(parts[0], parts[1]);
      }
    }
    return values;
  }

  private static int parseLimit(String rawLimit) {
    if (rawLimit == null || rawLimit.isBlank()) {
      return 0;
    }
    return Integer.parseInt(rawLimit.trim());
  }

  private static double ratio(int numerator, int denominator) {
    if (denominator == 0) {
      return 0.0;
    }
    return (double) numerator / denominator;
  }

  private enum EvaluationMode {
    ONNX_ONLY("onnx-only"),
    ONNX_NRC("onnx-nrc"),
    ONNX_NRC_DEPECHE("onnx-nrc-depeche");

    private final String displayName;

    EvaluationMode(String displayName) {
      this.displayName = displayName;
    }

    static EvaluationMode from(String raw) {
      if (raw == null || raw.isBlank()) {
        return null;
      }
      String normalized = raw.trim().toLowerCase();
      for (EvaluationMode mode : values()) {
        if (mode.displayName.equals(normalized)) {
          return mode;
        }
      }
      throw new IllegalArgumentException("Unknown evaluation mode: " + raw);
    }

    String displayName() {
      return displayName;
    }
  }
}
