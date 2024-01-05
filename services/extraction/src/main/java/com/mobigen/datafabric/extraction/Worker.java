package com.mobigen.datafabric.extraction;

import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MariaDBMetadata;
import com.mobigen.datafabric.extraction.dataSourceMetadata.MinioMetadata;
import com.mobigen.datafabric.extraction.model.DataMetadata;
import com.mobigen.datafabric.extraction.model.DataModel;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.util.ArrayList;

public class Worker {
    private final DataModel dataModel;

    public Worker(String id) {
        this.dataModel = getDataModel(id);
    }

    /**
     * DataModel의 메타데이터를 추출하고 저장하는 함수
     */
    public void upsertMetadata() {
        var location = this.dataModel.getDataLocation().getStorageId();
        TargetConfig target = null;
        // location을 활용하여 자동으로 어떤 extract를 사용할 지 선택하는 방법 구축
        Extract metaExtract = switch (TargetConfig.storageType) {
            case MINIO -> new MinioMetadata(target);
            case HDFS -> null;
            case MARIADB -> new MariaDBMetadata(target);
            case POSTGRESQL -> null;
            case HWP -> null;
        };
        try {
            var metadatas = metaExtract.extract();
            save(metadatas);
        } catch (Exception e) {
            // todo
        }
    }

    /**
     * dataModel의 DataModel을 저장하는 함수
     * rdbms와 OpenSearch에 동시에 저장하는 함수
     *
     * @param metadata
     */
    public void save(Metadata metadata) {
        var id = this.dataModel.getId();
        var metadataMap = metadata.getMetadata();
        var metadatas = new ArrayList<DataMetadata>();
        for (var key : metadataMap.keySet()) {
            var dataMetadata = new DataMetadata();
            dataMetadata.setId(id);
            dataMetadata.setKey(key);
            dataMetadata.setValue(metadataMap.get(key));
            // TODO dataMetadata.set_system();
            metadatas.add(dataMetadata);
        }

        this.dataModel.setDataMetadata(metadatas);
        // TODO dataLayer의 save 기능 사용
    }

    /**
     * Id를 가지고 등록되어 있는 DataModel 혹은 새 DataModel을 가져오는 함수
     * dataModel은 dataLocation, dataMetadata등을 모두 포함할 수 있는 Class로 만들어야 할 것이다.
     *
     * @param id dataModel's Id
     * @return
     */
    public DataModel getDataModel(String id) {
        // TODO DataLayer의 Load 기능 활용
        return new DataModel();
    }
}
