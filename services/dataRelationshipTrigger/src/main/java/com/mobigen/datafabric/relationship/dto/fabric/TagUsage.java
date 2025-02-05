package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.openmetadata.schema.type.TagLabel;

@Getter
@Setter
@Entity
@Table(name = "tag_usage")
public class TagUsage {
    @EmbeddedId
    private TagUsageId id;

    @Column(name = "tagFQN", nullable = false)
    private String tagFQN;

    @Column(name = "labelType", nullable = false)
    private TagLabel.LabelType labelType;

    @Column(name = "state", nullable = false)
    private TagLabel.State state;
}