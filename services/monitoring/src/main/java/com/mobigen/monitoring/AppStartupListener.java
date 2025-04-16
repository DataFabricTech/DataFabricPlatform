package com.mobigen.monitoring;

import com.mobigen.monitoring.config.ServiceTypeConfig;
import com.mobigen.monitoring.repository.MetadataRepository;
import com.mobigen.monitoring.service.scheduler.DynamicSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppStartupListener {

    private final DynamicSchedulerService dynamicSchedulerService;
    private final MetadataRepository metadataRepository;
    private final ServiceTypeConfig serviceTypeConfig;

    public AppStartupListener(
            final DynamicSchedulerService dynamicSchedulerService,
            final MetadataRepository metadataRepository,
            final ServiceTypeConfig serviceTypeConfig
    ) {
        this.dynamicSchedulerService = dynamicSchedulerService;
        this.metadataRepository = metadataRepository;
        this.serviceTypeConfig = serviceTypeConfig;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        log.debug("[APP] Loading database types from metadata");

        metadataRepository.findAllTypes()
                .forEach(serviceTypeConfig::addType);

        log.debug("[APP] Loaded types: {}", serviceTypeConfig.getTypes());

        log.debug("[APP] Run first monitoring");

        dynamicSchedulerService.runMonitoring();

        log.debug("[APP] END first monitoring");
    }
}
