package com.mobigen.datafabric.dataLayer.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

@Getter
@Setter
public class SearchModel {
    private int totalSize;
    private LinkedList<DataCatalogModel> dataCatalogModelList;
    private LinkedList<StorageModel> StorageModelList;
}
