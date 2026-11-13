package com.pgmmers.radar.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 单体融合后的事件处理线程池：引擎事件结果写入（ES 等）独立线程池，
 * 避免与管理端请求互相争抢资源。
 *
 * @author feihu.wang
 */
@Configuration
public class AsyncConfig {

    @Bean("riskResultExecutor")
    public Executor riskResultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("risk-result-");
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        // 队列满时由调用线程执行，避免事件结果丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
