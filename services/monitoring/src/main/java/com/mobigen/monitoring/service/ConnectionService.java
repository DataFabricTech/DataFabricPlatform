package com.mobigen.monitoring.service;

import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.response.ResponseTimesResponseDto;
import com.mobigen.monitoring.dto.response.ConnectionStatusSummaryResponseDto;
import com.mobigen.monitoring.repository.ServicesConnectResponseRepository;
import com.mobigen.monitoring.repository.ServicesRepository;
import com.mobigen.monitoring.vo.ResponseTimeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Services getConnectStatus(final Optional<Services> serviceOpt, final List<ConnectionHistory> connectionHistories) {
        return serviceOpt.map(
                services -> Services.builder()
                        .serviceID(services.getServiceID())
                        .name(services.getName())
                        .displayName(services.getDisplayName())
                        .serviceType(services.getServiceType())
                        .ownerName(services.getOwnerName())
                        .createdAt(services.getCreatedAt())
                        .updatedAt(services.getUpdatedAt())
                        .deleted(services.isDeleted())
                        .connectionStatus(services.getConnectionStatus())
                        .connections(null)
                        .connectionHistories(connectionHistories)
                        .build()
        ).orElse(null);
    }

    public ResponseTimesResponseDto getAvgResponseTimes(final boolean deleted, final PageRequest pageRequest) {
        final Long count = getCount();

        final List<ResponseTimeVo> response = getConnectionAvgResponseTime(deleted, pageRequest);

        return ResponseTimesResponseDto.builder()
                .responseTimes(response)
                .totalSize(count)
                .build();
    }

    public Object getRecentResponseTime(final boolean deleted, final PageRequest pageRequest) {
        Long totalCount = getCount();

        final List<ResponseTimeVo> data = servicesConnectResponseRepository.findRecResponseTimeResponse(deleted, pageRequest);

        return ResponseTimesResponseDto.builder()
                .responseTimes(data)
                .totalSize(totalCount)
                .build();
    }

    public List<ResponseTimeVo> getResponseTimes(final UUID serviceId, final PageRequest pageRequest) {
        return servicesConnectResponseRepository.findAvgResponseTimeResponse(serviceId, pageRequest);
    }

    private List<ResponseTimeVo> getConnectionAvgResponseTime(boolean deleted, PageRequest pageRequest) {
        return servicesConnectResponseRepository.findAvgResponseTimeResponse(deleted, pageRequest);
    }

    private Long getCount() {
        return servicesConnectResponseRepository.count();
    }

}
