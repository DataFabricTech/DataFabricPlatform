package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetadataService {
    private final MetadataRepository metadataRepository;

    public Long getRecentCollectedTime() {
        Optional<String> recentCollectedTime = metadataRepository.getRecentCollectedTime();

        return recentCollectedTime.isPresent() ? Long.parseLong(recentCollectedTime.get()) : null;
    }
}
