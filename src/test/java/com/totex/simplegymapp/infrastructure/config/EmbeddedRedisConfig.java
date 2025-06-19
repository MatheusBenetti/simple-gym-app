package com.totex.simplegymapp.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * Configuração do Redis embeddado para testes
 * Arquivo: src/test/java/com/totex/simplegymapp/config/EmbeddedRedisConfig.java
 */
@TestConfiguration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            redisServer = new RedisServer(6370); // Porta diferente para não conflitar
            redisServer.start();
        } catch (Exception e) {
            // Se a porta estiver ocupada, tenta outra
            try {
                redisServer = new RedisServer(6371);
                redisServer.start();
            } catch (Exception ex) {
                System.err.println("Não foi possível iniciar Redis embeddado: " + ex.getMessage());
                // Continua sem Redis para testes que não dependem de cache
            }
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6370);
    }
}