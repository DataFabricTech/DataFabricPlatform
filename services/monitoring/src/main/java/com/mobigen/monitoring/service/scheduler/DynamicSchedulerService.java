package com.mobigen.monitoring.service.scheduler;

import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.repository.*;
import com.mobigen.monitoring.service.ModelService;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import com.mobigen.monitoring.utils.UnixTimeUtil;
import com.mobigen.monitoring.vo.Target;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * service 모니터링 시간을 동적으로 바꾸고 관리할 수 있도록 관리하는 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicSchedulerService {
    // service
    private final DatabaseManagementService databaseManagementService;
    private final ModelService modelService;

    // repository
    private final ServicesRepository servicesRepository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;
    private final ConnectionDaoRepository connectionDaoRepository;
    private final ConnectionHistoryRepository connectionHistoryRepository;
    private final SlowQueryStatisticRepository slowQueryStatisticRepository;

    // open metadata 에서 받아온 데이터들을 가지고 있는 공통 변수
    private final ServiceModelRegistry serviceModelRegistry;

    @Value("${monitoring.history.delete.period}")
    private Long deletePeriod;

    // thread pool
    // TODO thread pool 크기를 동적으로 변경
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * application 이 실행되고 호출되는 함수
     */
    public void runMonitoring() {
        final List<Services> services = servicesRepository.findAllByDeletedIsFalseAndMonitoringIsTrue();

        // 모든 서비스들을 schedule 로 등록
        for (Services service : services) {
            schedule(
                    Target.builder()
                            .serviceId(service.getServiceID().toString())
                            .period(new AtomicInteger(service.getMonitoringPeriod()))
                            .build(),
                    run(service.getServiceID().toString())
            );
        }
    }

    /**
     * schedule 등록
     */
    public void schedule(Target target, Runnable taskLogic) {
        cancel(target.getServiceId());

        // runnable 로 실행
        final ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(
                taskLogic,
                0L,
                target.getPeriod().get(),
                TimeUnit.SECONDS);

        // schedule 중인 task 를 관리하기 위해 저장
        scheduledTasks.put(target.getServiceId(), future); // Map 에 저장
    }

    /**
     * Scheduling 취소 함수
     */
    public void cancel(String serviceId) {
        ScheduledFuture<?> future = scheduledTasks.remove(serviceId);

        // thread 에 cancel 요청 전달
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * service id 를 받아서 schedule 등록하는 함수
     */
    public void addSchedule(String serviceId, int monitoringPeriodInSeconds) {
        Target target = Target.builder()
                .serviceId(serviceId)
                .period(new AtomicInteger(monitoringPeriodInSeconds))
                .build();

        schedule(target, run(serviceId));
    }

    /**
     * 실제 모니터링 비즈니스 로직
     */
    public Runnable run(final String serviceId) {
        return () -> {
            log.debug("[START] MONITORING service: {}", serviceId == null ? "all" : serviceId);

            // data update
            log.debug("[FETCH] Fetching service info from fabric server");

            // 현재 가지고 있는 데이터를 업데이트하기 위해
            modelService.getServiceListFromFabricServer();

            // database service 일 경우
            if (serviceModelRegistry.getDatabaseServices().get(serviceId) != null) {
                log.debug("[SCHEDULING] Update database info: {}", serviceId);

                databaseManagementService.updateDatabaseInfo(serviceId);
            } else {
                // database, storage service 둘다 없을 경우
                log.error("[SCHEDULING] Update database info failed: {}", serviceId);

                throw new CustomException("Service not found");
            }

            log.debug("[END] MONITORING service: {}", serviceId == null ? "all" : serviceId);
        };
    }

    public Object getTasks() {
        return scheduledTasks.keySet();
    }

    @Scheduled(cron = "0 * 12 * * *")
    @Transactional
    public void deleteMonitoring() {
        log.debug("[START] DELETE MONITORING");

        long period = deletePeriod * 24L * 60 * 60 * 1000; // milliseconds 로 변경

        long now = UnixTimeUtil.getCurrentMillis();
        long threshold = now - period;

        // 계산 이상함 현재 시간 - milliseconds 보다 작은거
        connectionDaoRepository.deleteOlderThan(threshold);
        connectionHistoryRepository.deleteOlderThan(threshold);
        monitoringHistoryRepository.deleteOlderThan(threshold);
        slowQueryStatisticRepository.deleteOlderThan(threshold);

        log.debug("[END] DELETE MONITORING");
    }
}
