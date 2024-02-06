package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataAutoAdd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DataAutoAddRepository extends JpaRepository<DataAutoAdd, UUID> {
}
