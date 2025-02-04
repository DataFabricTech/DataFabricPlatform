package com.mobigen.datafabric.relationship.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InteractionData {
    public static final String[] columns = new String[]{
            "UserID", "DataID", "Type", "Value"};
    private String userId;
    private String dataId;
    private InteractionType type;
    private Integer value;

    public String[] getData() {
        return new String[]{userId, dataId, String.valueOf(type.getValue()), String.valueOf(value)};
    }
}
