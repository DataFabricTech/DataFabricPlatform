package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.dto.response.IngestionHistoryResponseDto;
import com.mobigen.monitoring.repository.IngestionHistoryRepository;
import com.mobigen.monitoring.vo.IngestionHistoryVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IngestionHistoryService {
    private final IngestionHistoryRepository ingestionHistoryRepository;

    public Long getCount() {
        return ingestionHistoryRepository.count();
    }

    public IngestionHistoryResponseDto getIngestionHistory(final PageRequest pageRequest) {
        final Long totalCount = getCount();
        final List<IngestionHistoryVo> ingestionHistoryResponse = ingestionHistoryRepository.findIngestionHistoryResponse(pageRequest);

        return IngestionHistoryResponseDto.builder()
                .totalCount(totalCount)
                .ingestionHistories(ingestionHistoryResponse)
                .build();
    }
}
