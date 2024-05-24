package com.mobigen.dolphin.repository.openmetadata;

import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface OMRepository extends JpaRepository<DBServiceEntity, UUID> {
}
