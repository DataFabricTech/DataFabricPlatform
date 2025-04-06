package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ConnectionDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConnectionDaoRepository extends JpaRepository<ConnectionDao, String> {
    public void deleteByServiceID(final UUID serviceID);
}