package com.emotion.emotionService.infrastructure.context;

import com.emotion.emotionService.config.ContextProperties;
import com.emotion.emotionService.domain.model.DialogueContext;
import com.emotion.emotionService.domain.service.ContextCacheRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisContextCacheRepository implements ContextCacheRepository {

  private static final String KEY_PREFIX = "emotion:dialog:";

  private final RedisTemplate<String, DialogueContext> redisTemplate;
  private final ContextProperties contextProperties;

  @Override
  public Optional<DialogueContext> findBySessionId(UUID sessionId) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(sessionId)));
  }

  @Override
  public void save(DialogueContext context) {
    redisTemplate.opsForValue().set(
        buildKey(context.sessionId()),
        context,
        contextProperties.ttl()
    );
  }

  @Override
  public void delete(UUID sessionId) {
    redisTemplate.delete(buildKey(sessionId));
  }

  private String buildKey(UUID sessionId) {
    return KEY_PREFIX + sessionId;
  }
}

