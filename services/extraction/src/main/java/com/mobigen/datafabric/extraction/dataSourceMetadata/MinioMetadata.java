package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.extraction.CommonMetadataExtraction;
import com.mobigen.datafabric.extraction.extraction.TikaExtraction;
import com.mobigen.datafabric.extraction.model.DataFormat;
import com.mobigen.datafabric.extraction.model.StructuredType;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import com.mobigen.datafabric.extraction.model.*;
import org.apache.tika.exception.UnsupportedFormatException;

import java.util.HashMap;
import java.util.Map;

public class MinioMetadata implements Extract, ExtractAdditional, ExtractExtend {
    String NAME = "MINIO";
    TargetConfig target;
    Metadata metadata = new Metadata();

    public MinioMetadata(TargetConfig target) {
        this.metadata.metadata = new HashMap<>();
        this.target = target;
    }

    @Override
    public Metadata extract() {
        try {
            extractDefault();
            extractAdditional();
            extractExtend();
        }  catch (UnsupportedFormatException e) {
            throw new RuntimeException(e); // todo
        }

        return this.metadata;
    }

    @Override
    public void extractDefault() throws UnsupportedFormatException {
        Map<String, String> defaultMeta = new HashMap<>(); // extract minio

//        var name = defaultMeta.get("name");
//        var modifiedAt = Long.parseLong(defaultMeta.get("modifiedAt"));
//        var type = switch (defaultMeta.get("type").toLowerCase()) {
//            case "csv", "structured etc" -> StructuredType.STRUCTURED;
//            case "xsl", "unstructured etc" -> StructuredType.SEMI_STRUCTURED;
//            default -> StructuredType.UN_STRUCTURED;
//        };
//
//        var dataFormat = switch (defaultMeta.get("dataFormat").toLowerCase()) {
//            case "xsl" -> DataFormat.EXCEL;
//            case "hwp" -> DataFormat.HWP;
//            case "csv" -> DataFormat.CSV;
//            case "word" -> DataFormat.WORD;
//            case "table" -> throw new UnsupportedFormatException("no table");
//            default -> DataFormat.UNKNOWN;
//        };
//
//        var size = Long.parseLong(defaultMeta.get("size"));

        for (var i: defaultMeta.keySet()) {
            this.metadata.metadata.put(i, defaultMeta.get(i));
        }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {
        switch (TargetConfig.dataFormat) {
            case HWP -> {
                var tika = new TikaExtraction();
                var additionalMeta = tika.extract(this.target.getTarget());

                for (var i: additionalMeta.keySet()) {
                    this.metadata.metadata.put(i, additionalMeta.get(i));
                }
            }
            case EXCEL -> {
                var tika = new TikaExtraction();
                var additionalMeta = tika.extract(this.target.getTarget());

                var sheetNames = new String[]{additionalMeta.get("sheetName")};
                for (var i: additionalMeta.keySet()) {
                    this.metadata.metadata.put(i, additionalMeta.get(i));
                }
            }
            case TABLE -> throw new UnsupportedFormatException("not Table");
            default -> {
                // TODO WORD, CSV, UNKNOWN
                // not use TABLE
            }
        }
    }

    @Override
    public void extractExtend() throws UnsupportedFormatException{
        switch (TargetConfig.dataFormat) {
            case HWP -> {
                var common = new CommonMetadataExtraction();
                var customMeta = common.extract(this.target.getTarget());

                for (var i: customMeta.keySet()) {
                    this.metadata.metadata.put(i, customMeta.get(i));
                }
            }
            case EXCEL -> {
                var common = new CommonMetadataExtraction();
                var customMeta = common.extract(this.target.getTarget());

                for (var i: customMeta.keySet()) {
                    this.metadata.metadata.put(i, customMeta.get(i));
                }
            }
            case WORD, CSV, UNKNOWN -> {
            }
            default -> throw new UnsupportedFormatException("not Table");
        };
    }
}
