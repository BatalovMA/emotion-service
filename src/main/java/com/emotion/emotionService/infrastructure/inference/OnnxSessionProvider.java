package com.emotion.emotionService.infrastructure.inference;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
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
public class OnnxSessionProvider {

  private OrtEnvironment environment;
  private OrtSession session;

  @PostConstruct
  public void init() {
    try {
      environment = OrtEnvironment.getEnvironment();
      Path modelPath = copyModelToTemp();
      session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize ONNX session", e);
    }
  }

  private Path copyModelToTemp() throws IOException {
    try (InputStream inputStream = getClass().getResourceAsStream("/model/model.onnx")) {
      if (inputStream == null) {
        throw new IllegalStateException("Missing /model/model.onnx resource");
      }
      Path tempFile = Files.createTempFile("emotion-model-", ".onnx");
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      tempFile.toFile().deleteOnExit();
      return tempFile;
    }
  }
}
