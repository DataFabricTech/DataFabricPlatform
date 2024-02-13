package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataFormatOption;
import dto.compositeKeys.DataFormatOptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataFormatOptionRepository extends JpaRepository<DataFormatOption, DataFormatOptionKey> {
}
