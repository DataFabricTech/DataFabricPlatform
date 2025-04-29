package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.domain.ModelRegistration;
import com.mobigen.monitoring.dto.response.ModelRegistrationResponseDto;
import com.mobigen.monitoring.repository.ModelRegistrationRepository;
import com.mobigen.monitoring.vo.ModelRegistrationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelRegistrationService {
    private final ModelRegistrationRepository modelRegistrationRepository;

    public void save(final ModelRegistration build) {
        modelRegistrationRepository.save(build);
    }

    public Long getCount() {
        return modelRegistrationRepository.count();
    }

    public ModelRegistrationResponseDto getAllModelRegistration(final boolean deleted, final PageRequest pageRequest) {
        final Page<ModelRegistrationVo> modelRegistration = modelRegistrationRepository.findModelRegistration(deleted, pageRequest);

        return ModelRegistrationResponseDto.builder()
                .models(modelRegistration.getContent())
                .totalCount(modelRegistration.getTotalElements())
                .build();
    }
}
