package com.mobigen.dolphin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        /* application.yml
        spring:
          task:
            execution:
              pool:
                core-size: 5
                max-size: 5
                queue-capacity: 5
                keep-alive: 30s
         */
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(500);
        executor.setQueueCapacity(5);
        executor.setKeepAliveSeconds(30);  // thread pool 내의 thread 개수가 corePoolSize 초과인 상태 일 때, 대기 상태의 thread 가 종료되기까지 대기 시간
        executor.setThreadNamePrefix("dolphin-async-");
        executor.initialize();

//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        //AbortPolicy: TaskRejectedException을 발생시키며 종료한다.
        //CallerRunsPolicy: 스레드풀을 호출한 스레드에서 처리한다. (톰캣에서 스레드풀을 호출했다면, 톰캣 스레드가 요청을 처리한다.)
        //DiscardPolicy: 해당 요청들을 무시한다.
        //DiscardOldestPolicy: 큐에 있는 가장 오래된 요청을 삭제하고 새로운 요청을 받아들인다. (queueCapacity가 0인 경우 StackOverFlowError가 발생한다.)
        return executor;
    }
}
