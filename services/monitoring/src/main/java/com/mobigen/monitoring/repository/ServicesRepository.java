package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.vo.ServicesResponse;
import com.mobigen.monitoring.enums.ConnectionStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServicesRepository extends JpaRepository<Services, UUID> {
    public long countServicesByDeletedIsFalse();

    public long countByConnectionStatusAndDeletedIsFalse(ConnectionStatus connectionStatus);

    @Query(nativeQuery = true, value = "select service_id as serviceID, service_name as serviceName, service_display_name as serviceDisplayName, " +
            "service_type as serviceType, owner_name as ownerName, created_at as createdAt, deleted, " +
            "connection_status as connectionStatus " +
            "from services " +
            "where deleted = ?1")
    public List<ServicesResponse> findServiceResponse(boolean deleted, Pageable pageRequest);

    List<Services> findAllByDeletedIsFalse();
}
