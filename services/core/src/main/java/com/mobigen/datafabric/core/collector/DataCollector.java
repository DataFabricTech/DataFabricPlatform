package com.mobigen.datafabric.core.collector;

import com.mobigen.datafabric.share.protobuf.StorageOuterClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    String getQuery();

    List<StorageOuterClass.StorageBrowseData> parse(ResultSet rs) throws SQLException;
}
