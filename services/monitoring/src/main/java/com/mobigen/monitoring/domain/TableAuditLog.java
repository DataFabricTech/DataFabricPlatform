package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Table(name = "table_audit_log")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TableAuditLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    private UUID id;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "insert_num")
    private Integer insertNum;

    @Column(name = "delete_num")
    private Integer deleteNum;

    @Column(name = "update_num")
    private Integer updateNum;

    @Column(name = "update_at")
    private Long updateAt;
}
