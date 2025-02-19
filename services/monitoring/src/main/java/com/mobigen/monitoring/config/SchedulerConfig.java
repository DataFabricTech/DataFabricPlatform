package com.mobigen.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfig {
    private String collectExpression = "0 0/30 * * * *";
    private String saveExpression = "0 0/5 * * * *";
    private String deleteExpression = "0 30 2 * * *";

    @Bean
    public TaskScheduler serviceCollectTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("serviceCollectTaskScheduler-");
        return taskScheduler;
    }

    @Bean
    public TaskScheduler ingestionCollectTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("ingestionCollectTaskScheduler-");
        return taskScheduler;
    }

    @Bean
    public TaskScheduler saveTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("saveTaskScheduler-");
        return taskScheduler;
    }

    @Bean
    public TaskScheduler deleteTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("deleteTaskScheduler-");
        return taskScheduler;
    }
}
