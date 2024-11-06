package com.mobigen.datafabric.relationship.trigger;

import com.mobigen.datafabric.relationship.dto.dolphin.FusionModel;
import com.mobigen.datafabric.relationship.dto.fabric.TableEntity;
import com.mobigen.datafabric.relationship.repository.dolphin.FusionModelRepository;
import com.mobigen.datafabric.relationship.repository.fabric.TableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SampleService {
    private final TableRepository tableRepository;
    private final FusionModelRepository fusionModelRepository;

    public SampleService(TableRepository tableRepository, FusionModelRepository fusionModelRepository) {
        this.tableRepository = tableRepository;
        this.fusionModelRepository = fusionModelRepository;
    }

    public String getSample() {
        return "Hello World!";
    }

    public String testGetTableData() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TableEntity> tables = tableRepository.findAll(pageRequest);
        StringBuilder sb = new StringBuilder();
        for (TableEntity table : tables) {
            log.info("table: {}", table.getName());
            sb.append(table.getName());
        }
        return sb.toString();
    }

    public String testGetFusionData() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<FusionModel> fusionModels = fusionModelRepository.findAll(pageRequest);
        StringBuilder sb = new StringBuilder();
        for (FusionModel model: fusionModels) {
            log.info("model: {}", model.getFullyqualifiedname());
            sb.append(model.getFullyqualifiedname());
        }
        return sb.toString();
    }
}
