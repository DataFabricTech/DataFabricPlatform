package com.mobigen.monitoring.service;

import com.mobigen.monitoring.domain.ConnectionDao;
import com.mobigen.monitoring.dto.response.ServiceConnectionHistoryResponseDto;
import com.mobigen.monitoring.dto.response.ServicesResponseDto;
import com.mobigen.monitoring.repository.ConnectionDaoRepository;
import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.response.ResponseTimesResponseDto;
import com.mobigen.monitoring.dto.response.ConnectionStatusSummaryResponseDto;
import com.mobigen.monitoring.repository.ConnectionHistoryRepository;
import com.mobigen.monitoring.repository.ServicesConnectResponseRepository;
import com.mobigen.monitoring.repository.ServicesRepository;
import com.mobigen.monitoring.vo.ResponseTimeVo;
import com.mobigen.monitoring.vo.ServiceVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mobigen.monitoring.enums.ConnectionStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {
    private final ConnectionDaoRepository connectionDaoRepository;
    private final ConnectionHistoryRepository connectionHistoryRepository;
    private final ServicesConnectResponseRepository servicesConnectResponseRepository;
    private final ServicesRepository servicesRepository;

    public ConnectionStatusSummaryResponseDto getConnectionStatusSummary() {
        return ConnectionStatusSummaryResponseDto.builder()
                .total(servicesRepository.countServicesByDeletedIsFalse())
                .connected(servicesRepository.countByConnectionStatusAndDeletedIsFalse(CONNECTED))
                .disconnected(servicesRepository.countByConnectionStatusAndDeletedIsFalse(DISCONNECTED))
                .connectError(servicesRepository.countByConnectionStatusAndDeletedIsFalse(CONNECT_ERROR))
                .build();
    }

    public ServiceConnectionHistoryResponseDto getConnectStatus(final Optional<Services> serviceOpt, Page<ConnectionHistory> connectionHistories) {
        return serviceOpt.map(
                services -> ServiceConnectionHistoryResponseDto.builder()
                        .data(
                                ServiceVo.builder()
                                        .serviceID(services.getServiceID())
                                        .name(services.getName())
                                        .displayName(services.getDisplayName())
                                        .serviceType(services.getServiceType())
                                        .createdAt(services.getCreatedAt())
                                        .updatedAt(services.getUpdatedAt())
                                        .deleted(services.isDeleted())
                                        .connectionStatus(services.getConnectionStatus())
                                        .connectionHistories(connectionHistories.getContent())
                                        .build()
                                )
                        .totalCount(connectionHistories.getTotalElements())
                        .build()
        ).orElse(null);
    }

    public ResponseTimesResponseDto getAvgResponseTimes(final boolean deleted, final PageRequest pageRequest) {
        final Page<ResponseTimeVo> response = getConnectionAvgResponseTime(deleted, pageRequest);

        return ResponseTimesResponseDto.builder()
                .responseTimes(response.getContent())
                .totalSize(response.getTotalElements())
                .build();
    }

    public Object getRecentResponseTime(final boolean deleted, final PageRequest pageRequest) {
        final Page<ResponseTimeVo> data = servicesConnectResponseRepository.findRecResponseTimeResponse(deleted, pageRequest);

        return ResponseTimesResponseDto.builder()
                .responseTimes(data.getContent())
                .totalSize(data.getTotalElements())
                .build();
    }

    public List<ResponseTimeVo> getResponseTimes(final UUID serviceId, final PageRequest pageRequest) {
        return servicesConnectResponseRepository.findAvgResponseTimeResponse(serviceId, pageRequest);
    }

    private Page<ResponseTimeVo> getConnectionAvgResponseTime(boolean deleted, PageRequest pageRequest) {
        return servicesConnectResponseRepository.findAvgResponseTimeResponse(deleted, pageRequest);
    }

    public void saveAllConnection(final List<ConnectionDao> connections) {
        connectionDaoRepository.saveAll(connections);
    }

    @Transactional
    public void saveAllConnectionHistory(final List<ConnectionHistory> connectionHistories) {
        connectionHistoryRepository.saveAll(connectionHistories);
    }
}
