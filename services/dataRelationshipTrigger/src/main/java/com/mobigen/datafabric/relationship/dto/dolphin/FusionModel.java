package com.mobigen.datafabric.relationship.dto.dolphin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "fusion_model")
public class FusionModel {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "fullyqualifiedname")
    private String fullyqualifiedname;

    @Column(name = "modelidofom")
    private UUID modelidofom;

    @Column(name = "trinomodelname")
    private String trinomodelname;

    @Column(name = "job_id")
    private String jobId;
}