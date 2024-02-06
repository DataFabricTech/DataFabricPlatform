package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataTypeOption;
import dto.compositeKeys.DataTypeOptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataTypeOptionRepository extends JpaRepository<DataTypeOption, DataTypeOptionKey> {
}
