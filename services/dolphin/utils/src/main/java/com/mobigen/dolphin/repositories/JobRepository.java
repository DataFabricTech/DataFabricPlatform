package com.mobigen.dolphin.repositories;

import com.mobigen.dolphin.models.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
}
