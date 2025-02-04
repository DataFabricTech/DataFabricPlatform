package com.mobigen.datafabric.relationship.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FusionData {

    public static final String[] columns = new String[]{
            "QueryID", "DataID"};
    private String queryId;
    private String dataId;

    public String[] getData() {
        return new String[]{queryId, dataId};
    }
}
