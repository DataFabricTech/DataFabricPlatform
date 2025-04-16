package com.mobigen.monitoring.config;

import com.mobigen.monitoring.domain.Metadata;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.repository.MetadataRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Configuration
public class ServiceTypeConfig {
    private final List<String> types;
    private final MetadataRepository metadataRepository;

    public ServiceTypeConfig(final MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.types = new ArrayList<>();
    }

    @Transactional
    public void addType(String type) {
        if (!types.contains(type)) {
            metadataRepository.save(
                    Metadata.builder()
                            .metadataName(String.format("service.%s", type))
                            .metadataValue(type)
                    .build()
            );

            this.types.add(type);
        } else {
            throw new CustomException(ResponseCode.DFM8000, String.format("Database type [%s] is already exists", type), type);
        }
    }

    @Transactional
    public void removeType(final String type) {
        if (types.contains(type)) {
            final Metadata metadata = metadataRepository.findById(String.format("service.%s", type)).orElseThrow(
                    () -> new CustomException(ResponseCode.DFM8001, String.format("Database type [%s] is not exists", type), type)
            );

            metadataRepository.delete(metadata);

            this.types.remove(type);
        } else {
            throw new CustomException(ResponseCode.DFM8001, String.format("Database type [%s] is not exists", type), type);
        }
    }

    public String findType(final String type) {
        if (types.contains(type)) {
            return type;
        } else {
            return null;
        }
    }
}
