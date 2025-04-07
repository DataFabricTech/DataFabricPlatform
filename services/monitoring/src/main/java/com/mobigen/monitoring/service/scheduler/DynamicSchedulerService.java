package com.mobigen.monitoring.service.scheduler;

import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.repository.ServicesRepository;
import com.mobigen.monitoring.service.ModelService;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import com.mobigen.monitoring.vo.DynamicScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/* *
 * TODO
 *  1. 계속 돈다.
 *  2. map 확인해서 cron 이 맞는지 확인
 *  3. Thread pool 에 작업 던지기
 *  4. 모니터링 시작
 *  5. thread pool 은 10으로 유지
 *  6. 실제 모니터링 작업은 async 비동기
 * */
@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, DynamicScheduledTask> tasks = new ConcurrentHashMap<>();
    private final DatabaseManagementService databaseManagementService;
    private final ServicesRepository servicesRepository;
    private final ServiceModelRegistry serviceModelRegistry;
    private final ModelService modelService;

    @Scheduled(fixedRate = 1000)
    private void schedule() {
        // Map 확인 후 cron 규칙에 맞는지 확인 후에 실행
        for(DynamicScheduledTask task : tasks.values()) {
            // run monitoring
            run("serviceId");
        }
    }

    public void addTask(String serviceId, Runnable task, String cronExpression) {
        if (tasks.containsKey(serviceId)) {
            removeTask(serviceId); // 기존 task 제거
        }

        CronTrigger trigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

        DynamicScheduledTask scheduledTask = new DynamicScheduledTask();
        scheduledTask.setServiceId(serviceId);
        scheduledTask.setTask(task);
        scheduledTask.setFuture(future);
        scheduledTask.setCronExpression(cronExpression);

        tasks.put(serviceId, scheduledTask);
    }

    public void removeTask(String taskId) {
        DynamicScheduledTask scheduledTask = tasks.remove(taskId);
        if (scheduledTask != null && scheduledTask.getFuture() != null) {
            scheduledTask.getFuture().cancel(true);
        }
    }

    public void updateTask(String taskId, String newCron) {
        DynamicScheduledTask scheduledTask = tasks.get(taskId);
        if (scheduledTask != null) {
            addTask(taskId, scheduledTask.getTask(), newCron); // 기존 task로 재등록
        }
    }

    public Set<String> getRunningTaskIds() {
        return tasks.keySet();
    }

    public void run(final String serviceId) {
        /* *
         * TODO monitoring task 실행 전 hashmap 업데이트
         **/

        // data update
        modelService.getServiceListFromFabricServer();

        // database service 일 경우
        if (serviceModelRegistry.getDatabaseServices().get(serviceId) != null) {
            final GetDatabasesResponseDto databaseServiceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);

            // cpu, memory, request, slow query
            // table update => connection, connection_history, metadata, model_registration, services
            // connection -> saveConnections
            // model-registration
            // saveServicesDatabase -> 서비스들을 저자아혹 싶어함
            final Services service = servicesRepository.findById(UUID.fromString(databaseServiceInfo.getId())).orElseThrow(
                    () -> new CustomException("Service is not found")
            );

            final Boolean connection = databaseManagementService.checkDatabaseConnection(databaseManagementService.getDatabaseConnectionRequest(service.getServiceID().toString()));

            // service -> model 정보
            // connection_history -> 연결되었는지 확인
            // connection -> query 수행 시간
            // metadata -> 몇개의 서비스를 가져왔는지
            // model registration -> 등록한 model, 실제 model 개수


        } else if (serviceModelRegistry.getStorageServices().get(serviceId) != null) {
            final GetObjectStorageResponseDto storageServiceInfo = serviceModelRegistry.getStorageServices().get(serviceId);

        } else {
            // database, storage service 둘다 없을 경우
            throw new CustomException("Service not found");
        }
    }
}
