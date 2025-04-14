package com.mobigen.monitoring;

import com.mobigen.monitoring.service.scheduler.DynamicSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppStartupListener {
    private final DynamicSchedulerService dynamicSchedulerService;

    public AppStartupListener(final DynamicSchedulerService dynamicSchedulerService) {
        this.dynamicSchedulerService = dynamicSchedulerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        // application 시작 시 돌기
         log.debug("[APP] Run first monitoring");

        dynamicSchedulerService.runMonitoring();

        log.debug("[APP] Run first monitoring");
    }
}
