package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
import com.mobigen.dolphin.entity.response.ModelDto;
import com.mobigen.dolphin.repository.openmetadata.OMRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Service
public class ModelService {
    private final OMRepository omRepository;
    private final TrinoRepository TrinoRepository;

    public List<ModelDto> getModels() {
        return TrinoRepository.getModelList();
    }

    public void getConnectorInfo() {
        var data = omRepository.findAll();
        System.out.println(data.stream().map(DBServiceEntity::toString).collect(Collectors.joining()));
    }
}
