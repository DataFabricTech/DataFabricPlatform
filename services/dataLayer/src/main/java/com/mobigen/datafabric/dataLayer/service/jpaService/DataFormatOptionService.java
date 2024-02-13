package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.DataFormatOption;
import dto.compositeKeys.DataFormatOptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DataFormatOptionService extends SyncService<DataFormatOption, DataFormatOptionKey> {
    public DataFormatOptionService(JpaRepository<DataFormatOption, DataFormatOptionKey> repository) {
        super(repository);
    }
}
