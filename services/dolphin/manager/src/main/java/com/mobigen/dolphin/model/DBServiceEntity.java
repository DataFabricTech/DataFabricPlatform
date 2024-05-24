package com.mobigen.dolphin.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@Entity
@Table(name = "dbservice_entity")
public class DBServiceEntity {
    @Id
    private UUID id;
    private String name;
    private String serviceType;
    private String json;
    private Long updatedAt;
    private String updatedBy;
    private Boolean deleted;
    private String nameHash;
}
