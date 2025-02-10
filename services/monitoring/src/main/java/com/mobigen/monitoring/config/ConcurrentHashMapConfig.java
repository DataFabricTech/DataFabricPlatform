package com.mobigen.monitoring.config;

import com.mobigen.monitoring.service.timer.TaskInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ConcurrentHashMapConfig {
    @Bean
    public ConcurrentHashMap<String, TaskInfo> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
