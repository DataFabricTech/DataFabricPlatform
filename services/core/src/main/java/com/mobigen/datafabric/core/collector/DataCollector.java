package com.mobigen.datafabric.core.collector;

import com.mobigen.datafabric.share.protobuf.StorageOuterClass;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface DataCollector {
    void setDepth(Integer depth);

    void setPath(String path);

    List<StorageOuterClass.StorageBrowseData> collect(String url, Map<String, Object> options, Properties properties, String driver);
}
