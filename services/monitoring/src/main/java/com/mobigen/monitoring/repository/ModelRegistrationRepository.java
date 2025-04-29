package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ModelRegistration;
import com.mobigen.monitoring.vo.ModelRegistrationVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModelRegistrationRepository extends JpaRepository<ModelRegistration, UUID> {
    @Query(nativeQuery = true, value = "select s.service_id as serviceId, s.service_name as serviceName, s.service_display_name as serviceDisplayName, m.om_model_count as openMetadataModelCount, m.model_count as modelCount " +
            "from services as s left join model_registration as m on m.service_id = s.service_id " +
            "where s.deleted = ?1")
    public Page<ModelRegistrationVo> findModelRegistration(boolean deleted, PageRequest pageRequest);

    public void deleteByServiceId(UUID serviceId);
}
