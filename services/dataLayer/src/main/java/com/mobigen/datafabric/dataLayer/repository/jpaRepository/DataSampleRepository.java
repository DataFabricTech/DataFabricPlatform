package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataSample;
import dto.compositeKeys.DataSampleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSampleRepository extends JpaRepository<DataSample, DataSampleKey> {
}
