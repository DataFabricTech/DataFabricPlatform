package com.mobigen.dolphin.repositories;

import com.mobigen.dolphin.models.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface MappingRepository extends JpaRepository<MappingEntity, UUID> {
    MappingEntity findByHiveTable(String hiveTable);

    List<MappingEntity> findAllByNameIn(List<String> names);
}
