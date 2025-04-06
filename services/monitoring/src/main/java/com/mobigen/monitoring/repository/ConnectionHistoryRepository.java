package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.vo.ConnectionHistoryVo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectionHistoryRepository extends JpaRepository<ConnectionHistory, UUID> {
    public List<ConnectionHistory> findByServiceIDOrderByUpdatedAtDesc(UUID serviceID, Pageable pageable);

    public void deleteAllByUpdatedAtLessThan(Long threshold);

    @Query(nativeQuery = true, value = "select sch.service_id as serviceId, s.service_name as serviceName, s.service_display_name as serviceDisplayName, s.service_type as serviceType, sch.connection_status as connectionStatus " +
            "from connection_history as sch left join services as s on sch.service_id = s.service_id ")
    public List<ConnectionHistoryVo> getConnectionHistories(PageRequest pageRequest);

    @Query(nativeQuery = true, value = "select sch.service_id as serviceID, s.service_name as serviceName, s.service_display_name as serviceDisplayName, s.service_type as serviceType, sch.connection_status as connectionStatus " +
            "from connection_history as sch left join services as s on sch.service_id = s.service_id " +
            "where sch.service_id = ?1")
    public List<ConnectionHistoryVo> findConnectHistoryResponse(UUID serviceId, PageRequest pageRequest);

    public void deleteByServiceID(UUID serviceID);
}
