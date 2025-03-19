package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.config.JsonComparator;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.vo.ServicesResponse;
import com.mobigen.monitoring.repository.ServicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mobigen.monitoring.enums.ConnectionStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ServicesService {
    private final ServicesRepository servicesRepository;

    public long countByConnectionStatusIsConnected() {
        return servicesRepository.countByConnectionStatusAndDeletedIsFalse(CONNECTED);
    }

    public long countByConnectionStatusIsDisconnected() {
        return servicesRepository.countByConnectionStatusAndDeletedIsFalse(DISCONNECTED);
    }

    public long countByConnectionStatusIsConnectError() {
        return servicesRepository.countByConnectionStatusAndDeletedIsFalse(CONNECT_ERROR);
    }

    public Long getCount() {
        return servicesRepository.countServicesByDeletedIsFalse();
    }

    public List<Services> getAllService() {
        return servicesRepository.findAll();
    }

    public List<Object> getServiceResponse(boolean deleted, PageRequest pageRequest) {
//        return servicesRepository.findServiceResponse(deleted, pageRequest);
        return null;
    }

    public Optional<Services> getServices(UUID serviceID) {
        return servicesRepository.findById(serviceID);
    }

    public List<Services> saveServices(List<Services> servicesList) {
        return servicesRepository.saveAll(servicesList);
    }

    public List<ServicesResponse> getServices(final boolean deleted, final PageRequest pageRequest) {
        return servicesRepository.findServiceResponse(deleted, pageRequest);
    }

    public Boolean isServiceTableEmpty() {
        return servicesRepository.findAll().isEmpty();
    }

    public Services saveService(final Services service) {
        return servicesRepository.save(service);
    }
}
