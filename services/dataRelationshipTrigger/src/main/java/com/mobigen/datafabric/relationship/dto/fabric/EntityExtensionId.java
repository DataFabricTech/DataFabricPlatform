package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class EntityExtensionId implements Serializable {
    private static final long serialVersionUID = 4233061507716471537L;
    @NotNull
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull
    @Column(name = "extension", nullable = false)
    private String extension;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EntityExtensionId entity = (EntityExtensionId) o;
        return Objects.equals(this.extension, entity.extension) &&
                Objects.equals(this.id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, id);
    }

}