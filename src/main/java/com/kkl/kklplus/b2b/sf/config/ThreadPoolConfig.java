package com.kkl.kklplus.b2b.sf.config;

import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/***
 *  线程池
 */
@Configuration
public class ThreadPoolConfig {

    @Autowired
    private B2BSFProperties sfProperties;

    @Bean
    ThreadPoolTaskExecutor cancelThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(sfProperties.getThreadPool().getCorePoolSize());
        executor.setKeepAliveSeconds(sfProperties.getThreadPool().getKeepAliveSeconds());
        executor.setMaxPoolSize(sfProperties.getThreadPool().getMaxPoolSize());
        executor.setQueueCapacity(sfProperties.getThreadPool().getQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

}
