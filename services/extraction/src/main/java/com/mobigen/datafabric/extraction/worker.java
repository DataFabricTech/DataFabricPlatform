package com.mobigen.datafabric.extraction;

import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MariaDBMetadata;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MinioMetadata;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import static dto.enums.AdaptorType.MINIO;

public class worker {
    public static void main(String[] args) {
        TargetConfig target = null;
        // 여기서 자동으로 어떤 extract를 사용할 지 선택하는 방법 구축
        Extract metaExtract = switch (MINIO) {
            case MINIO -> new MinioMetadata(target);
            case MARIADB -> new MariaDBMetadata(target);
            default -> null;
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
