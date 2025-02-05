package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class ProfilerDataEntityId implements Serializable {
    private static final long serialVersionUID = -2455843433720582410L;
    @Column(name = "entityFQNHash", nullable = false)
    private String entityFQNHash;
    @Column(name = "extension", nullable = false)
    private String extension;
    @Column(name = "operation")
    private String operation;
    @Column(name = "timestamp")
    private Long timestamp;
}
