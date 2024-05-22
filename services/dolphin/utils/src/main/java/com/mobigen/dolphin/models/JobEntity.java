package com.mobigen.dolphin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job")
public class JobEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;
    @Enumerated(value = EnumType.STRING)
    private JobStatus status;
    private String query;
    private String query2;
    private Timestamp created;
    private Timestamp updated;

    @Column(name = "result_name")
    private String resultName;
    @Column(name = "result_path")
    private String resultPath;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "worker_id", referencedColumnName = "id")
    private WorkerEntity worker;

    public enum JobStatus {
        DENIED,
        ACCEPT,
        RUNNING,
        SUCCEED,
        FINISHED,
        FAILED,
        CANCELED
    }

}
