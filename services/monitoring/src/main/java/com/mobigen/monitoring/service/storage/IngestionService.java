package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.domain.Ingestion;
import com.mobigen.monitoring.repository.IngestionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class IngestionService {
    private final IngestionsRepository ingestionsRepository;

    public List<Ingestion> getIngestionList() {
        return ingestionsRepository.findAll();
    }

    public Optional<Ingestion> getIngestion(UUID ingestionID) {
        return ingestionsRepository.findById(ingestionID);
    }

    public void saveIngestionList(List<Ingestion> ingestionDTOList) {
        ingestionsRepository.saveAll(ingestionDTOList);
    }

    public void deleteAll() {
        ingestionsRepository.deleteAll();
    }
}