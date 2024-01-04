package com.mobigen.datafabric.extraction;

import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MariaDBMetadata;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MinioMetadata;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

public class worker {
    public static void main(String[] args) {
        TargetConfig target = null;
        // 여기서 자동으로 어떤 extract를 사용할 지 선택하는 방법 구축
        Extract metaExtract = switch (TargetConfig.storageType) {
            case MINIO -> new MinioMetadata(target);
            case HDFS -> null;
            case MARIADB -> new MariaDBMetadata(target);
            case POSTGRESQL -> null;
            case HWP -> null;
        };
        try {
            var metadata = metaExtract.extract();
            save(metadata);
        } catch (Exception e) {
            // todo
        }
    }

    public static void save(Metadata metadataOld) {
    }

}
