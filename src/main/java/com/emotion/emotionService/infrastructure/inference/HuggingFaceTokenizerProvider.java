package com.emotion.emotionService.infrastructure.inference;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class HuggingFaceTokenizerProvider {

  private HuggingFaceTokenizer tokenizer;

  @PostConstruct
  public void init() {
    try {
      Path tokenizerPath = copyTokenizerToTemp();
      tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load tokenizer", e);
    }
  }

  private Path copyTokenizerToTemp() throws IOException {
    try (InputStream inputStream = getClass().getResourceAsStream("/model/tokenizer.json")) {
      if (inputStream == null) {
        throw new IllegalStateException("Missing /model/tokenizer.json resource");
      }
      Path tempFile = Files.createTempFile("emotion-tokenizer-", ".json");
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      tempFile.toFile().deleteOnExit();
      return tempFile;
    }
  }
}
