package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.ConnectionDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionDaoRepository extends JpaRepository<ConnectionDao, String> {
}