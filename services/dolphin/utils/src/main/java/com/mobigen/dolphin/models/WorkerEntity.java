package com.mobigen.dolphin.models;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;
    private byte health;
    @Enumerated(value = EnumType.STRING)
    private WorkerStatus status;

    public enum WorkerStatus {
        RUNNING,
        DIED
    }

}
