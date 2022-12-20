package io.github.shelltea.lettuce;

import io.lettuce.core.RedisCommandTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时执行PING命令。
 */
@Slf4j
public class ScheduledPing implements Runnable {
    private static final int MAX_PING_FAILED_TIMES = 3;
    private final List<LettuceConnectionFactory> redisConnectionList;
    private final Map<String, AtomicInteger> hostPingFailed = new HashMap<>();

    public ScheduledPing(List<LettuceConnectionFactory> redisConnectionList) {
        this.redisConnectionList = redisConnectionList;

        for (LettuceConnectionFactory lettuceConnectionFactory : this.redisConnectionList) {
            hostPingFailed.put(lettuceConnectionFactory.getHostName(), new AtomicInteger(0));
        }
    }

    @Override
    public void run() {
        log.info("定时执行PING命令");
        for (LettuceConnectionFactory connectionFactory : this.redisConnectionList) {
            final String hostName = connectionFactory.getHostName();
            final int port = connectionFactory.getPort();
            final AtomicInteger pingFailed = hostPingFailed.get(hostName);

            try {
                log.debug("ping {}:{},ping fail count:{}", hostName, port, pingFailed);
                final String pingResult = connectionFactory.getConnection().ping();
                pingFailed.set(0);
                log.debug("ping {}:{} --> {},ping fail count:{}", hostName, port, pingResult, pingFailed);
            } catch (RedisCommandTimeoutException ex) {
                if (pingFailed.incrementAndGet() == MAX_PING_FAILED_TIMES) {
                    log.warn("PING timeout {} times. Reset connection.", MAX_PING_FAILED_TIMES, ex);
                    connectionFactory.resetConnection();
                    pingFailed.set(0);
                }
            }
        }
        log.info("执行PING结束");
    }
}
