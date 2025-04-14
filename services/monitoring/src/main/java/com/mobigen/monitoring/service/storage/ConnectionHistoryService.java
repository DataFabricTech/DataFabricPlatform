package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.dto.response.AllConnectionHistoryResponseDto;
import com.mobigen.monitoring.repository.ConnectionHistoryRepository;
import com.mobigen.monitoring.vo.ConnectionHistoryVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionHistoryService {
    private final ConnectionHistoryRepository connectionHistoryRepository;
    private final MetadataService metadataService;

    public List<ConnectionHistory> getConnectionHistories(UUID serviceID, PageRequest pageRequest) {
        return connectionHistoryRepository.findByServiceIDOrderByUpdatedAtDesc(serviceID, pageRequest);
    }

    public AllConnectionHistoryResponseDto getAllConnectionHistory(final PageRequest pageRequest) {
        final Long recentCollectedTime = metadataService.getRecentCollectedTime();

        final List<ConnectionHistoryVo> connectionHistoriesResponse = connectionHistoryRepository.getConnectionHistories(pageRequest);

        return AllConnectionHistoryResponseDto.builder()
                .recentCollectedTime(recentCollectedTime)
                .connectionHistories(connectionHistoriesResponse)
                .build();
    }

    public List<ConnectionHistoryVo> getConnectionHistory(final UUID serviceId, final PageRequest pageRequest) {
        return connectionHistoryRepository.findConnectHistoryResponse(serviceId, pageRequest);
    }
}
