package io.github.shelltea.lettuce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通过定时任务发送PING命令检测Lettuce连接的有效性。
 */
@Slf4j
public class PingConnectionHandler implements DisposableBean {
    private static final int INITIAL_DELAY = 1;
    private static final int DELAY = 1;
    private static final int AWAIT_TIMEOUT = 3;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    PingConnectionHandler(List<LettuceConnectionFactory> redisConnectionList) {
        executor.scheduleWithFixedDelay(new ScheduledPing(redisConnectionList), INITIAL_DELAY, DELAY, TimeUnit.MINUTES);
    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down PingConnectionHandler");
        executor.shutdown();

        if (!executor.awaitTermination(AWAIT_TIMEOUT, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
