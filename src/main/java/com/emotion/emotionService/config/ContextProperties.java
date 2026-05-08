package com.emotion.emotionService.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "emotion.context")
public record ContextProperties(Duration ttl, int windowSize) {}
