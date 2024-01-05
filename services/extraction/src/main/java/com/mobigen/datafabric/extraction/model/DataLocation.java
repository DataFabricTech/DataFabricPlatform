package com.mobigen.datafabric.extraction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name ="data_location")
public class DataLocation {
    // TODO Share로 이동할 Class
    @Id
    private String id;
    @Id
    private String storageId;
    private String Path;
    private String name;
    private String scope;
    private String sheetName;
    private String separator;
    private String cellRange;
    private String begin;
    private String end;
}
