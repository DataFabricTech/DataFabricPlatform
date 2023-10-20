package com.mobigen.datafabric.dataLayer.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Setter
@Getter
public class RecentSearchesModel {
    @Nullable
    private String userId;

    private String[] recentSearches;
}
