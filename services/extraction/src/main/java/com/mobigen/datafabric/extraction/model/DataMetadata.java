package com.mobigen.datafabric.extraction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

@Entity
@Table(name ="data_metadata")
@Setter
public class DataMetadata {
    // TODO Share로 이동할 Class
    @Id
    private String id;

    private boolean is_system;
    private String key;
    private String value;
}
