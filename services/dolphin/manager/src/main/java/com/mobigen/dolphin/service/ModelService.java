package com.mobigen.dolphin.service;

import com.mobigen.dolphin.model.response.ModelDto;
import com.mobigen.dolphin.repository.trino.JdbcTrinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final JdbcTrinoRepository jdbcTrinoRepository;

    public List<ModelDto> getModels() {
        return jdbcTrinoRepository.getModelList();
    }
}
