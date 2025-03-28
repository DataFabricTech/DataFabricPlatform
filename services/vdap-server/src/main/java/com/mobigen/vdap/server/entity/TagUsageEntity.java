package com.mobigen.vdap.server.entity;

import com.mobigen.vdap.schema.type.TagLabel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "tag_usage_key",
                columnNames = {"source", "source_id", "tag_id", "target_type", "target_id"}
        )
)
@IdClass(TagUsageId.class)
public class TagUsageEntity {
    @Id
    @Column(name = "source", nullable = false)
    private Integer source;         // 부모 데이터 타입 0 : classification, 1 : glossary, 2 : glossary term

    @Id
    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private String tagId;

    @Id
    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Id
    @Column(name = "target_id", nullable = false)
    private String targetId;
}