package io.github.shelltea.lettuce;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
public class PingAutoConfiguration {
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public PingConnectionHandler init(List<RedisTemplate<?, ?>> redisTemplates) {
        log.info("Initializing PingConnectionHandler, Detected {} RedisTemplate", redisTemplates.size());

        List<LettuceConnectionFactory> redisConnectionList = new ArrayList<>();
        for (RedisTemplate<?, ?> redisTemplate : redisTemplates) {
            final RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory instanceof LettuceConnectionFactory) {
                redisConnectionList.add((LettuceConnectionFactory) connectionFactory);
            }
        }

        return new PingConnectionHandler(redisConnectionList);
    }
}
