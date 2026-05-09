package com.emotion.emotionService.infrastructure.inference;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtSession;
import com.emotion.emotionService.domain.model.EmotionResult;
import com.emotion.emotionService.domain.model.Message;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class OnnxEmotionInferenceEngine implements EmotionInferenceEngine {

  private static final int TOP_EMOTION_LIMIT = 3;

  private final OnnxSessionProvider sessionProvider;
  private final HuggingFaceTokenizerProvider tokenizerProvider;

  @Override
  public EmotionResult analyze(Message message) {
    try {
      HuggingFaceTokenizer tokenizer = tokenizerProvider.getTokenizer();
      Encoding encoding = tokenizer.encode(message.text());

      long[] inputIds = encoding.getIds();
      long[] attentionMask = encoding.getAttentionMask();

      try (OnnxTensor inputTensor =
              OnnxTensor.createTensor(
                  sessionProvider.getEnvironment(),
                  LongBuffer.wrap(inputIds),
                  new long[] {1, inputIds.length});
          OnnxTensor attentionTensor =
              OnnxTensor.createTensor(
                  sessionProvider.getEnvironment(),
                  LongBuffer.wrap(attentionMask),
                  new long[] {1, attentionMask.length});
          OrtSession.Result result =
              sessionProvider
                  .getSession()
                  .run(Map.of("input_ids", inputTensor, "attention_mask", attentionTensor))) {

        float[][] logits = (float[][]) result.get(0).getValue();
        float[] probabilities = softmax(logits[0]);
        int bestIndex = argmax(probabilities);
        List<String> emotions = topKEmotions(probabilities);

        EmotionLabels emotionLabel = EmotionLabels.LABELS.get(bestIndex);
        double sentiment = mapEmotionToSentiment(emotionLabel);

        return EmotionResult.builder()
            .speaker(message.speaker())
            .emotion(emotions)
            .sentiment(sentiment)
            .intensity(Math.abs(sentiment))
            .confidence(probabilities[bestIndex])
            .build();
      }
    } catch (Exception e) {
      throw new IllegalStateException("ONNX inference failed", e);
    }
  }

  private List<String> topKEmotions(float[] probabilities) {
    int limit = Math.min(OnnxEmotionInferenceEngine.TOP_EMOTION_LIMIT, probabilities.length);
    return IntStream.range(0, probabilities.length)
        .boxed()
        .sorted((a, b) -> Float.compare(probabilities[b], probabilities[a]))
        .limit(limit)
        .map(index -> EmotionLabels.LABELS.get(index).toString())
        .collect(Collectors.toList());
  }

  private int argmax(float[] values) {
    int maxIndex = 0;
    for (int i = 1; i < values.length; i++) {
      if (values[i] > values[maxIndex]) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  private float[] softmax(float[] logits) {
    float max = Float.NEGATIVE_INFINITY;
    for (float v : logits) {
      max = Math.max(max, v);
    }

    float sum = 0f;
    float[] exps = new float[logits.length];
    for (int i = 0; i < logits.length; i++) {
      exps[i] = (float) Math.exp(logits[i] - max);
      sum += exps[i];
    }

    for (int i = 0; i < exps.length; i++) {
      exps[i] /= sum;
    }

    return exps;
  }

  private double mapEmotionToSentiment(EmotionLabels emotion) {
    return switch (emotion) {
      case JOY, LOVE, ADMIRATION, APPROVAL, GRATITUDE, OPTIMISM, RELIEF, PRIDE, EXCITEMENT -> 0.7;
      case ANGER,
          ANNOYANCE,
          DISAPPOINTMENT,
          DISAPPROVAL,
          DISGUST,
          FEAR,
          GRIEF,
          REMORSE,
          SADNESS,
          EMBARRASSMENT,
          NERVOUSNESS ->
          -0.7;
      default -> 0.0;
    };
  }
}
