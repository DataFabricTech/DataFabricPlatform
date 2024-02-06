package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ModelService extends JpaService<Model, UUID> {
    public ModelService(JpaRepository<Model, UUID> repository) {
        super(repository);
    }
}
