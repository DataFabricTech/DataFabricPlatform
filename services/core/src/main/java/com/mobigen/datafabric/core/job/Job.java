package com.mobigen.datafabric.core.job;

import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private JobType type;
    private String storage;
    private String dataCatalog;
}
