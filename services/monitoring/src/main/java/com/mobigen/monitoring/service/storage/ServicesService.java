package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.domain.*;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.enums.ServiceEventEnum;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.repository.*;
import com.mobigen.monitoring.service.scheduler.DynamicSchedulerService;
import com.mobigen.monitoring.utils.UnixTimeUtil;
import com.mobigen.monitoring.vo.ConnectionInfo;
import com.mobigen.monitoring.vo.ServicesResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mobigen.monitoring.enums.ConnectionStatus.*;
import static com.mobigen.monitoring.enums.ServiceEventEnum.*;
import static com.mobigen.monitoring.enums.ServiceModelType.DATABASE_SERVICE;
import static com.mobigen.monitoring.enums.ServiceModelType.getServiceModelType;

@Slf4j
@RequiredArgsConstructor
@Service
public class ServicesService {
    private final ServicesRepository servicesRepository;
    private final ConnectionDaoRepository connectionRepository;
    private final ConnectionHistoryRepository connectionHistoryRepository;
    private final MetadataRepository metadataRepository;
    private final ModelRegistrationRepository modelRegistrationRepository;
    private final ServiceModelRegistry serviceModelRegistry;
    private final DatabaseManagementServiceImpl databaseManagementService;
    private final DynamicSchedulerService dynamicSchedulerService;

    public Optional<Services> getServices(UUID serviceID) {
        return servicesRepository.findById(serviceID);
    }

    public List<ServicesResponse> getServices(final boolean deleted, final PageRequest pageRequest) {
        return servicesRepository.findServiceResponse(deleted, pageRequest);
    }

    /**
     * Open metadata 로부터 받은 이베트에 따른 처리
     * */
    @Transactional
    public Object handleServiceEvent(final String serviceId, final ServiceEventEnum serviceEventEnum, Boolean isHardDelete, String type) {
        if (serviceEventEnum.equals(CREATED) || serviceEventEnum.equals(UPDATED)) {
            final Long now = UnixTimeUtil.getCurrentMillis();
            ConnectionInfo connectionInfo;
            Services service;

            if (getServiceModelType(type).equals(DATABASE_SERVICE)) {
                GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);

                // service 등록
                service = servicesRepository.save(
                        Services.builder()
                                .serviceID(UUID.fromString(serviceId))
                                .name(serviceInfo.getName())
                                .displayName(serviceInfo.getDisplayName())
                                .serviceType(serviceInfo.getServiceType())
                                .createdAt(now)
                                .updatedAt(now)
                                .deleted(false)
                                .connectionStatus(serviceInfo.getConnectionStatus())
                                .build()
                );

                connectionInfo = ConnectionInfo.builder()
                        .connectionStatus(serviceInfo.getConnectionStatus())
                        .responseTime(serviceInfo.getResponseTime())
                        .build();
            } else {
                GetObjectStorageResponseDto serviceInfo = serviceModelRegistry.getStorageServices().get(serviceId);

                // service 등록
                service = servicesRepository.save(
                        Services.builder()
                                .serviceID(UUID.fromString(serviceId))
                                .name(serviceInfo.getName())
                                .displayName(serviceInfo.getDisplayName())
                                .serviceType(serviceInfo.getServiceType())
                                .createdAt(now)
                                .updatedAt(now)
                                .deleted(false)
                                .connectionStatus(serviceInfo.getConnectionStatus())
                                .build()
                );

                connectionInfo = ConnectionInfo.builder()
                        .connectionStatus(serviceInfo.getConnectionStatus())
                        .responseTime(serviceInfo.getResponseTime())
                        .build();
            }

            // connection, connection history 등록
            // connection entity 생성
            connectionRepository.save(
                    ConnectionDao.builder()
                            .executeAt(UnixTimeUtil.getCurrentMillis())
                            .queryExecutionTime(connectionInfo.getResponseTime())
                            .serviceID(UUID.fromString(serviceId))
                            .build()
            );

            // connection history
            connectionHistoryRepository.save(
                    ConnectionHistory.builder()
                            .connectionStatus(connectionInfo.getConnectionStatus())
                            .serviceID(UUID.fromString(serviceId))
                            .updatedAt(now)
                            .build()
            );

            // modelRegistration
            databaseManagementService.saveModelRegistration(service);

            final int models = serviceModelRegistry.getStorageServices().size() + serviceModelRegistry.getDatabaseServices().size();

            // metadata update
            metadataRepository.save(
                    Metadata.builder()
                            .metadataName("recent_collect_time")
                            .metadataValue(String.valueOf(models))
                            .build()
            );

            // monitoring 등록
            dynamicSchedulerService.addSchedule(serviceId, service.getMonitoringPeriod());

            return "Success create service";
        } else { // DELETED
            dynamicSchedulerService.cancel(serviceId);

            if (isHardDelete) {
                // hard delete
                servicesRepository.deleteById(UUID.fromString(serviceId));
                connectionHistoryRepository.deleteByServiceID(UUID.fromString(serviceId));
                connectionRepository.deleteByServiceID(UUID.fromString(serviceId));
                modelRegistrationRepository.deleteByServiceId(UUID.fromString(serviceId));

                Metadata metadata = metadataRepository.findById("recent_collected_time").orElseThrow(
                        () -> new CustomException("recent_collected_time is not found")
                );

                int modelNum = Integer.parseInt(metadata.getMetadataValue());

                metadata.setMetadataValue(String.valueOf(modelNum - 1));

                metadataRepository.save(metadata);
            } else {
                // soft delete
                // change service's deleted to true
                Services service = servicesRepository.findById(UUID.fromString(serviceId)).orElseThrow(
                        () -> new CustomException(ResponseCode.DFM6000, "service is not found: " + serviceId, serviceId)
                );

                service.setDeleted(true);

                servicesRepository.save(service);
            }

            return "Success soft delete";
        }
    }
}
