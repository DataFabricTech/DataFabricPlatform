package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.Connection;
import com.mobigen.monitoring.vo.ResponseTimeVo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServicesConnectResponseRepository extends JpaRepository<Connection, UUID> {
    @Query(nativeQuery = true, value = "select sc.service_id as serviceId, s.service_name as serviceName, s.service_display_name as serviceDisplayName ,sc.execute_at as executeAt, sc.execute_by as executeBy, sc.query_execution_time as queryExecutionTime " +
            "from connection as sc left join services as s on sc.service_id = s.service_id where s.deleted = ?1")
    public List<ResponseTimeVo> findAvgResponseTimeResponse(boolean deleted, PageRequest pageRequest);

    @Query(nativeQuery = true, value = "select sc1.service_id as serviceId, s.service_name as serviceName, " +
            "s.service_display_name as serviceDisplayName ,sc1.execute_at as executeAt, sc1.execute_by as executeBy, sc1.query_execution_time as queryExecutionTime " +
            "from connection as sc1 left join services as s on sc1.service_id = s.service_id where s.deleted = ?1 " +
            "and sc1.execute_at = (select max(sc2.execute_at) from connection as sc2 where sc1.service_id = sc2.service_id)")
    public List<ResponseTimeVo> findRecResponseTimeResponse(boolean deleted, PageRequest pageRequest);

    @Query(nativeQuery = true, value = "select sc.service_id as serviceID, s.service_name as serviceName, s.service_display_name as serviceDisplayName, sc.execute_at as executeAt, sc.execute_by as executeBy, sc.query_execution_time as queryExecutionTime " +
            "from connection as sc left join services as s on sc.service_id = s.service_id " +
            "where sc.service_id = ?1")
    public List<ResponseTimeVo> findAvgResponseTimeResponse(UUID serviceId, PageRequest pageRequest);
}
