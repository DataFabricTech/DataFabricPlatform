package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.domain.Metadata;
import com.mobigen.monitoring.repository.MetadataRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void save(final int size) {
        final Optional<Metadata> response = metadataRepository.findById("recent_collected_time");

        if (response.isPresent()) {
            Metadata metadata = response.get();

            metadata.setMetadataValue(String.valueOf(size));

            metadataRepository.save(response.get());
        } else {
            metadataRepository.save(
                    Metadata.builder()
                            .metadataName("recent_collected_time")
                            .metadataValue(String.valueOf(size))
                            .build()
            );
        }
    }
}
