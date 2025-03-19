package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metadata")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Metadata {
    @Id
    @Column(name = "metadata_name")
    private String metadataName;

    @Column(name = "metadata_value")
    private String metadataValue;
}
