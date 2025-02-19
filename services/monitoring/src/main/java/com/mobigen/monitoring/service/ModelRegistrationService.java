package com.mobigen.monitoring.service;

import com.mobigen.monitoring.domain.ModelRegistration;
import com.mobigen.monitoring.dto.response.ModelRegistrationResponseDto;
import com.mobigen.monitoring.repository.ModelRegistrationRepository;
import com.mobigen.monitoring.vo.ModelRegistrationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelRegistrationService {
    private final ModelRegistrationRepository modelRegistrationRepository;

    public void saveModelRegistrations(List<ModelRegistration> modelRegistrationList) {
        modelRegistrationRepository.saveAll(modelRegistrationList);
    }

    public List<Object> getModelRegistrations(boolean deleted, PageRequest pageRequest) {
        return null;
    }

    public Long getCount() {
        return modelRegistrationRepository.count();
    }

    public void deleteAll() {
        modelRegistrationRepository.deleteAll();
    }

    public ModelRegistrationResponseDto getAllModelRegistration(final boolean deleted, final PageRequest pageRequest) {
        final Long totalCount = getCount();
        final List<ModelRegistrationVo> modelRegistration = modelRegistrationRepository.findModelRegistration(deleted, pageRequest);

        return ModelRegistrationResponseDto.builder()
                .models(modelRegistration)
                .totalCount(totalCount)
                .build();
    }
}
