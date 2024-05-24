package com.mobigen.dolphin.entity.local;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@Entity
@Table(name = "worker")
public class WorkerEntity {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;
    private byte health;
    @Enumerated(value = EnumType.STRING)
    private WorkerStatus status;

    public enum WorkerStatus {
        RUNNING,
        DIED
    }

}
