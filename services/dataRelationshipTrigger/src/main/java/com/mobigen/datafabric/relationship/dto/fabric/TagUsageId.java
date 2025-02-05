package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.openmetadata.schema.type.TagLabel;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TagUsageId implements Serializable {
    private static final long serialVersionUID = -2455843433720582410L;
    @Column(name = "source", nullable = false)
    private TagLabel.TagSource source;

    @Column(name = "tagFQNHash")
    private String tagFQNHash;

    @Column(name = "targetFQNHash", length = 768)
    private String targetFQNHash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TagUsageId entity = (TagUsageId) o;
        return Objects.equals(this.source, entity.source) &&
                Objects.equals(this.tagFQNHash, entity.tagFQNHash) &&
                Objects.equals(this.targetFQNHash, entity.targetFQNHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, tagFQNHash, targetFQNHash);
    }
}
