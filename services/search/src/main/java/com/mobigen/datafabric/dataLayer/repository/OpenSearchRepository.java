package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.config.OpenSearchConfig;
import com.mobigen.datafabric.dataLayer.model.OpenSearchModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.libs.configuration.Config;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class OpenSearchRepository {
    private final OpenSearchClient client = getClient();
    private final OpenSearchConfig openSearchConfig;

    public OpenSearchRepository(OpenSearchConfig openSearchConfig) {
        this.openSearchConfig = openSearchConfig;
    }

    public OpenSearchClient getClient() {
        final HttpHost[] hosts = new HttpHost[]{ // TODO change using config
                new HttpHost("http", "192.168.107.28", 35000)
        };

        final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                .builder(hosts)
                .setMapper(new JacksonJsonpMapper())
                .build();
        return new OpenSearchClient(transport);
    }

    public LinkedList<String> search(List mustQueryArray, List<Query> shouldQueryArray) {
        var dataModelIds = new LinkedList<String>();
        try {
            // TODO must가 List여야만 가능했던 걸로 기억이 나는데, List<Query>를 다시 Test해보자.
            var hits = client.search(s -> s.index(openSearchConfig.getDataModelIndex())
                            .query(q -> q.bool(b -> b.minimumShouldMatch("1")
                                    .must(mustQueryArray)
                                    .should(shouldQueryArray)))
                    , OpenSearchModel.class).hits().hits();
            hits.forEach(hit -> dataModelIds.add(hit.source().getId()));
        } catch (Exception e) { // TODO
            e.printStackTrace();
        }

        return dataModelIds;
    }

    public Hit<RecentSearchesModel> search(String userId) {
        try {
            return client.search(s -> s.index(openSearchConfig.getRecentSearchesIndex())
                            .query(q -> q.match(
                                    m -> m.field("userId").query(FieldValue.of(userId))
                            )),
                    RecentSearchesModel.class).hits().hits().get(0);
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String[] getResentSearches(String userId) {
        try {
            return client.search(s -> s.index(openSearchConfig.getRecentSearchesIndex())
                    .query(q -> q.match(
                            m -> m.field("userId").query(FieldValue.of(userId))
                    )),
                    RecentSearchesModel.class).hits().hits().get(0).source().getRecentSearches();
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createIndex() {
        try {
            if (checkIndex(openSearchConfig.getDataModelIndex())) {
                // todo log start check datamodelindex create datamodelindex
                var properties = new HashMap<String, Property>();
                // todo properties -> meta는 필요 없음?
                for (var field : OpenSearchModel.class.getDeclaredFields()) {
                    if (field.getType() == String.class) {
                        properties.put(field.getName(), new Property.Builder()
                                .text(new TextProperty.Builder().fielddata(true).build()).build());
                    }
                }

                var mappings = new TypeMapping.Builder().properties(properties).build();

                client.indices().create(c -> c.index(openSearchConfig.getDataModelIndex()).mappings(mappings));
            }

            // todo log start check recentSearches create recentSearches
            if (checkIndex(openSearchConfig.getRecentSearchesIndex()))
                // todo log
                // todo recentsearches's schema 상관 없나? test 해볼것
                client.indices().create(c -> c.index(openSearchConfig.getRecentSearchesIndex()));
        } catch (OpenSearchException | IOException e) {
            // todo
            e.printStackTrace();
        }
    }

    public boolean checkIndex(String indexName) {
        try {
            return !client.indices().get(i -> i.index(indexName)).result().isEmpty();
        } catch (OpenSearchException | IOException e) {
            return false;
        }
    }


    public void insertDocument(OpenSearchModel openSearchModel) {
        try {
            client.index(i -> i.index(openSearchConfig.getDataModelIndex()).document(openSearchModel));
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }
    }

    public void insertDocument(RecentSearchesModel recentSearchesModel) {
        try {
            client.index(i -> i.index(openSearchConfig.getRecentSearchesIndex()).document(recentSearchesModel));
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDocument(OpenSearchModel openSearchModel, String id) {
        try {
            client.update(u -> u.index(openSearchConfig.getDataModelIndex())
                            .id(id)
                            .doc(openSearchModel)
                    , RecentSearchesModel.class);
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDocument(RecentSearchesModel recentSearchesModel, String id) {
        try {
            client.update(u -> u.index(openSearchConfig.getRecentSearchesIndex())
                            .id(id)
                            .doc(recentSearchesModel)
                    , RecentSearchesModel.class);
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDocument(String id) {
        try {
            var docId = client.search(s -> s.index(openSearchConfig.getDataModelIndex())
                    .query(q -> q.match(
                            m -> m.field("id").query(FieldValue.of(id))
                    )), OpenSearchModel.class).hits().hits().get(0).id();

            client.delete(d -> d.index(openSearchConfig.getDataModelIndex()).id(docId));
        } catch (OpenSearchException | IOException e) {
            e.printStackTrace();
        }

    }
}

