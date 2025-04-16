package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ConnectionDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ConnectionDaoRepository extends JpaRepository<ConnectionDao, String> {
    public void deleteByServiceID(final UUID serviceID);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM connection e WHERE e.execute_at > ?1")
    public void deleteOlderThan(long threshold);
}