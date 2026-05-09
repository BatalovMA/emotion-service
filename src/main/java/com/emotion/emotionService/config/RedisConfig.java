package com.emotion.emotionService.config;

import com.emotion.emotionService.domain.model.DialogueContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(ContextProperties.class)
public class RedisConfig {

  @Bean
  public RedisTemplate<String, DialogueContext> dialogueContextRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, DialogueContext> template = new RedisTemplate<>();

    JacksonJsonRedisSerializer<DialogueContext> serializer =
        new JacksonJsonRedisSerializer<>(DialogueContext.class);

    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);
    template.afterPropertiesSet();

    return template;
  }
}
