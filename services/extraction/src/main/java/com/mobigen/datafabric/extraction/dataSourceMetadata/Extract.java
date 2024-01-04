package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.model.Metadata;
import org.apache.tika.exception.UnsupportedFormatException;

public interface Extract {
    Metadata extract();
    void extractDefault() throws UnsupportedFormatException;
}
