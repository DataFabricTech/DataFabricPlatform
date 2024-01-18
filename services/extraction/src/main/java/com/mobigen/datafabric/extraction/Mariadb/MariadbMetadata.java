package com.mobigen.datafabric.extraction.Mariadb;

import com.mobigen.datafabric.extraction.RdbDefault.RdbExtractMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

public class MariadbMetadata extends RdbExtractMetadata {
    public MariadbMetadata(TargetConfig target) throws ClassNotFoundException {
        super(target);
    }
}