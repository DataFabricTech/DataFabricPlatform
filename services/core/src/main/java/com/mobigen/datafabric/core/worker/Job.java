package com.mobigen.datafabric.core.worker;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Job {
    private JobType type;
    private String storageId;
    private String dataCatalogId;

    public enum JobType {
        STORAGE_ADD,
        STORAGE_UPDATE,
        STORAGE_DELETE,
        AUTO_CREATE_DATA_CATALOG,
        STORAGE_SYNC,
        STORAGE_MONITORING,
        CREATE_DATA_CATALOG,
    }
}
