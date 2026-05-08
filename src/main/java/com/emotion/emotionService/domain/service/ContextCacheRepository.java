package com.emotion.emotionService.domain.service;

import com.emotion.emotionService.domain.model.DialogueContext;
import java.util.Optional;
import java.util.UUID;

public interface ContextCacheRepository {

  Optional<DialogueContext> findBySessionId(UUID sessionId);

  void save(DialogueContext context);

  void delete(UUID sessionId);
}

