package com.mobigen.datafabric.extraction.extraction;

import java.util.Map;

public interface Extract {
    Map<String, String> extract(Object input);
}
