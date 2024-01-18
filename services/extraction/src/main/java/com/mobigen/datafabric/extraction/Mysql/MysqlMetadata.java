package com.mobigen.datafabric.extraction.Mysql;

import com.mobigen.datafabric.extraction.RdbDefault.RdbExtractMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

public class MysqlMetadata extends RdbExtractMetadata {
    public MysqlMetadata(TargetConfig target) throws ClassNotFoundException {
        super(target);
    }
}
