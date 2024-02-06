package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.DataTypeOption;
import dto.compositeKeys.DataTypeOptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DataTypeOptionService extends JpaService<DataTypeOption, DataTypeOptionKey> {
    public DataTypeOptionService(JpaRepository<DataTypeOption, DataTypeOptionKey> repository) {
        super(repository);
    }
}
