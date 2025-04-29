package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ConnectionDao;
import com.mobigen.monitoring.vo.ResponseTimeVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServicesConnectResponseRepository extends JpaRepository<ConnectionDao, UUID> {
    @Query(nativeQuery = true, value =
            "SELECT sc.service_id AS serviceId, " +
                    "s.service_name AS serviceName, " +
                    "s.service_display_name AS serviceDisplayName, " +
                    "AVG(sc.query_execution_time) AS queryExecutionTime " +
                    "FROM connection AS sc " +
                    "LEFT JOIN services AS s ON sc.service_id = s.service_id " +
                    "WHERE s.deleted = ?1 " +
                    "GROUP BY sc.service_id, s.service_name, s.service_display_name",
            countQuery =
                    "SELECT COUNT(DISTINCT sc.service_id) " +
                            "FROM connection AS sc " +
                            "LEFT JOIN services AS s ON sc.service_id = s.service_id " +
                            "WHERE s.deleted = ?1"
    )
    public Page<ResponseTimeVo> findAvgResponseTimeResponse(boolean deleted, PageRequest pageRequest);

    @Query(nativeQuery = true, value = "select sc1.service_id as serviceId, s.service_name as serviceName, " +
            "s.service_display_name as serviceDisplayName ,sc1.execute_at as executeAt, sc1.query_execution_time as queryExecutionTime " +
            "from connection as sc1 left join services as s on sc1.service_id = s.service_id where s.deleted = ?1 " +
            "and sc1.execute_at = (select max(sc2.execute_at) from connection as sc2 where sc1.service_id = sc2.service_id)")
    public Page<ResponseTimeVo> findRecResponseTimeResponse(boolean deleted, PageRequest pageRequest);

    @Query(nativeQuery = true, value =
            "SELECT sc.service_id AS serviceID, " +
                    "s.service_name AS serviceName, " +
                    "s.service_display_name AS serviceDisplayName, " +
                    "sc.execute_at AS executeAt, " +
                    "sc.query_execution_time AS queryExecutionTime " +
                    "FROM connection AS sc " +
                    "LEFT JOIN services AS s ON sc.service_id = s.service_id " +
                    "WHERE sc.service_id = ?1",
            countQuery =
                    "SELECT COUNT(*) " +
                            "FROM connection AS sc " +
                            "LEFT JOIN services AS s ON sc.service_id = s.service_id " +
                            "WHERE sc.service_id = ?1"
    )
    public List<ResponseTimeVo> findAvgResponseTimeResponse(UUID serviceId, PageRequest pageRequest);
}
