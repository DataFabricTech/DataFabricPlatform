package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.extraction.TableExtraction;
import com.mobigen.datafabric.extraction.model.*;
import org.apache.tika.exception.UnsupportedFormatException;

import java.util.HashMap;

public class MariaDBMetadata implements Extract, ExtractAdditional{
    String NAME = "MariaDB";
    TargetConfig target;
    Metadata metadata = new Metadata();

    public MariaDBMetadata(TargetConfig target) {
        this.metadata.metadata = new HashMap<>();
        this.target = target;
    }

    @Override
    public Metadata extract() {
        try {
            extractDefault();
            extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }

        return this.metadata;
    }

    @Override
    public void extractDefault() throws UnsupportedFormatException {
        var defaultMeta = new HashMap<String, String>();

//        var name = defaultMeta.get("name");
//        var modifiedAt = Long.parseLong(defaultMeta.get("modifiedAt"));
//        var type = StructuredType.STRUCTURED;
//
//        if (!defaultMeta.get("dataformat").equals("table"))
//            throw new UnsupportedFormatException("no table");
//
//        var dataFormat = DataFormat.TABLE;
//
//        var size = Long.parseLong(defaultMeta.get("size"));

        for (var i: defaultMeta.keySet()) {
            this.metadata.metadata.put(i, defaultMeta.get(i));
        }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {
        var table = new TableExtraction();
        var additionalMeta = table.extract(this.target);

        for (var i: additionalMeta.keySet()) {
            this.metadata.metadata.put(i, additionalMeta.get(i));
        }
    }
}
