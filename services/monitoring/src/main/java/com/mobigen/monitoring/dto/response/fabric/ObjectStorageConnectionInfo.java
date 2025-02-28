package com.mobigen.monitoring.dto.response.fabric;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ObjectStorageConnectionInfo {
    private String type;
    private List<String> bucketNames;
    private MinioConfigInfo minioConfig;
}
