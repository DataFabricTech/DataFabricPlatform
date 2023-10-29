package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.model.OpenSearchModel;
import com.mobigen.libs.grpc.QueryResponseMessage;

public class MultiRepository{
    private final OpenSearchRepository openSearchRepository;
    private final RDBMSRepository rdbmsRepository;

    public MultiRepository(OpenSearchRepository openSearchRepository, RDBMSRepository rdbmsRepository) {
        this.openSearchRepository = openSearchRepository;
        this.rdbmsRepository = rdbmsRepository;
    }

    public QueryResponseMessage insert(String query) {
        rdbmsRepository.execute(query);
//        openSearchRepository.insertDocument(convertToOpenSearch(query));
        sync(query);
        return null;
    }

    public QueryResponseMessage delete(String query) {
        rdbmsRepository.execute(query);
//        openSearchRepository.deleteDocument(convertToSolr(query));
        sync(query);
        return null;
    }

    public QueryResponseMessage update(String query) {
        rdbmsRepository.execute(query);
//        openSearchRepository.updateDocument(convertToSolr(query));
        sync(query);
        return null;
    }


    public boolean sync(String query) {
        return false;
    }
}
