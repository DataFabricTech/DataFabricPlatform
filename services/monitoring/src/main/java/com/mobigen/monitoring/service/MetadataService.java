package com.mobigen.monitoring.service;

import com.mobigen.monitoring.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetadataService {
    private final MetadataRepository metadataRepository;

    public Long getRecentCollectedTime() {
        return Long.parseLong(metadataRepository.getRecentCollectedTime());
    }
}
