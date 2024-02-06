package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.DataAutoAdd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DataAutoAddService extends JpaService<DataAutoAdd, UUID> {
    public DataAutoAddService(JpaRepository<DataAutoAdd, UUID> repository) {
        super(repository);
    }
}
