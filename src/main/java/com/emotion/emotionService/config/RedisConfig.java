package com.emotion.emotionService.config;

import com.emotion.emotionService.domain.model.DialogueContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(ContextProperties.class)
public class RedisConfig {

  @Bean
  public RedisTemplate<String, DialogueContext> dialogueContextRedisTemplate(
      RedisConnectionFactory connectionFactory
  ) {
    RedisTemplate<String, DialogueContext> template = new RedisTemplate<>();
    ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    //TODO: resolve deprecated serializer
    Jackson2JsonRedisSerializer<DialogueContext> serializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, DialogueContext.class);

    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);
    template.afterPropertiesSet();

    return template;
  }
}
