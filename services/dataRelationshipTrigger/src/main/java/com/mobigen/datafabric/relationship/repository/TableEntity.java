package com.mobigen.datafabric.relationship.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "table_entity")
public class TableEntity extends org.openmetadata.schema.entity.data.Table {
}