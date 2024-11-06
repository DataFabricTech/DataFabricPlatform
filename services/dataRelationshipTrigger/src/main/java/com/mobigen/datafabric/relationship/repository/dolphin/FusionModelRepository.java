package com.mobigen.datafabric.relationship.repository.dolphin;

import com.mobigen.datafabric.relationship.dto.dolphin.FusionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FusionModelRepository extends CrudRepository<FusionModel, Long> {

    Page<FusionModel> findAll(Pageable pageable);

}
