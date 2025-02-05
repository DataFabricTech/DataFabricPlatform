package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class EntityRelationshipId implements Serializable {
    private static final long serialVersionUID = -6523727451001896881L;
    @Column(name = "fromId", nullable = false, length = 36)
    private String fromId;

    @Column(name = "toId", nullable = false, length = 36)
    private String toId;

    @Column(name = "relation", nullable = false)
    private Integer relation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EntityRelationshipId entity = (EntityRelationshipId) o;
        return Objects.equals(this.toId, entity.toId) &&
                Objects.equals(this.fromId, entity.fromId) &&
                Objects.equals(this.relation, entity.relation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toId, fromId, relation);
    }

}