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
    private Integer source;         // 0 : classification, 1 : glossary

    @Id
    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private String tagId;

    @Column(name = "label_type")
    private TagLabel.LabelType label_type;

    @Column(name = "state")
    private TagLabel.State state;

    @Id
    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Id
    @Column(name = "target_id", nullable = false)
    private String targetId;
}