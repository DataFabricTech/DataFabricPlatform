package com.mobigen.monitoring.service.monitoring;

import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.repository.ServicesRepository;
import com.mobigen.monitoring.service.ModelService;
import com.mobigen.monitoring.service.scheduler.DatabaseConnectionInfo;
import com.mobigen.monitoring.service.scheduler.MonitoringTask;
import com.mobigen.monitoring.service.scheduler.TaskInfo;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MonitoringService {
    private final Timer timer;
    private final MonitoringTask timerTask;
    private final ConcurrentHashMap<String, DatabaseConnectionInfo> tasks;
    private final DatabaseManagementService databaseManagementService;
    private final ServiceModelRegistry serviceModelRegistry;
    private final ServicesRepository servicesRepository;
    private final ModelService modelService;

    public MonitoringService(
            final DatabaseManagementService databaseManagementService,
            final ServiceModelRegistry serviceModelRegistry,
            final ServicesRepository servicesRepository, final ModelService modelService
    ) {
        this.databaseManagementService = databaseManagementService;
        this.serviceModelRegistry = serviceModelRegistry;
        this.servicesRepository = servicesRepository;
        this.modelService = modelService;
        this.tasks = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.timerTask = new MonitoringTask(this);
    }

    @Scheduled(fixedRate = 1000)
    public void monitoring() {

    }

    public Object addTask(TaskInfo taskInfo) {
        tasks.put(taskInfo.id(), taskInfo.databaseConnectionInfo());

        return "Successfully added task";
    }

    public Object removeTask(String taskId) {
        tasks.remove(taskId);
        return "Successfully removed task";
    }

    public Object start() {
        timer.schedule(timerTask, 0, 500);

        return "Successfully started";
    }

    public MonitoringTask saveMonitoringTask(String serviceId) {
        // serviceId로 service name 조회
        // 연결 상태
        // 데이터 수
        // 데이터 변경 감지 (table row, table update time, file update time, file size)
        // 평균 응답 시간
        // history

        return null;
    }

    public Object runMonitoring(final String serviceId) {
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


            return null;
        } else if (serviceModelRegistry.getStorageServices().get(serviceId) != null) {
            final GetObjectStorageResponseDto storageServiceInfo = serviceModelRegistry.getStorageServices().get(serviceId);

            return null;
        } else {
            // database, storage service 둘다 없을 경우
            throw new CustomException("Service not found");
        }
    }
}
