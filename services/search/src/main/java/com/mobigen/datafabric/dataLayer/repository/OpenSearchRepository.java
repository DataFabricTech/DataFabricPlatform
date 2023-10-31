package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.config.OpenSearchConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Deprecated
@Slf4j
public class OpenSearchRepository {
    private final OpenSearchClient client;
    private final OpenSearchConfig openSearchConfig;

    public OpenSearchRepository(OpenSearchConfig openSearchConfig) {
        this.openSearchConfig = openSearchConfig;
        this.client = getClient();
    }


    public OpenSearchClient getClient() throws OpenSearchException, NullPointerException {
        try {
            log.info("OpenSearch Server: {}:{}", openSearchConfig.getHost(), openSearchConfig.getPort());

            final HttpHost[] hosts = new HttpHost[]{
                    new HttpHost("http", openSearchConfig.getHost(), openSearchConfig.getPort())
            };

            final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                    .builder(hosts)
                    .setMapper(new JacksonJsonpMapper())
                    .build();
            return new OpenSearchClient(transport);
        } catch (OpenSearchException e) {
            log.error("[getClient] cause : {}, message : {}", e.getCause(), e.getMessage());
            throw e;
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public LinkedList<String> search(List<Query> mainQuery, List<Query> mustQuery, List<Query> shouldQuery)
            throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            var boolQuery = new BoolQuery.Builder();
            if (mainQuery != null && !mainQuery.isEmpty()) boolQuery.should(mainQuery);
            if (mustQuery != null && !mustQuery.isEmpty()) boolQuery.must(mustQuery);
            if (shouldQuery != null && !shouldQuery.isEmpty()) boolQuery.should(shouldQuery).minimumShouldMatch("2");
            else boolQuery.minimumShouldMatch("1");


            var hits = client.search(s -> s.index(openSearchConfig.getDataSetIndex())
                            .query(q -> q.bool(boolQuery.build()))
                    , DataCatalogModel.class).hits().hits();
            hits.forEach(hit -> {
                var id = hit.source().getId();
                ids.add(id.substring(1, id.length() - 1));
            });
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }

        return ids;
    }

    public Map<String, Aggregate> getFacet(List<Query> mainQuery, List<Query> mustQuery, List<Query> shouldQuery)
            throws OpenSearchException, IOException {
        log.info("[getFacet] start");
        try {
            var map = new HashMap<String, Aggregation>();
            for (var field : DataCatalogModel.class.getDeclaredFields()) {
                if (field.getType() == String.class) {
                    map.put(field.getName(),
                            new Aggregation.Builder().terms(t -> t.field(field.getName())).build());
                }
            }

            var boolQuery = new BoolQuery.Builder();
            if (mainQuery != null && !mainQuery.isEmpty()) boolQuery.should(mainQuery);
            if (mustQuery != null && !mustQuery.isEmpty()) boolQuery.must(mustQuery);
            if (shouldQuery != null && !shouldQuery.isEmpty()) boolQuery.should(shouldQuery).minimumShouldMatch("2");
            else boolQuery.minimumShouldMatch("1");

            return client.search(s -> s.index(openSearchConfig.getDataSetIndex()).size(1000)
                            .aggregations(map)
                            .query(q -> q.bool(boolQuery.build()))
                    , DataCatalogModel.class).aggregations();
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * @param shouldQueryArray
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public LinkedList<String> search(List<Query> shouldQueryArray) throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            var hits = client.search(s -> s.index(openSearchConfig.getStorageIndex())
                            .size(10000)
                            .query(q -> q.bool(b -> b.minimumShouldMatch("1").should(shouldQueryArray)))
                    , StorageModel.class).hits().hits();
            hits.forEach(hit -> {
                var id = hit.source().getId();
                ids.add(id.substring(1, id.length() - 1));
            });
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return ids;
    }

    public LinkedList<String> search() throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            // todo size -> config
            var hits = client.search(s -> s.index(openSearchConfig.getDataSetIndex())
                            .size(10000)
                    , DataCatalogModel.class).hits().hits();
            hits.forEach(hit -> {
                var id = hit.source().getId();
                ids.add(id.substring(1, id.length() - 1));
            });
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return ids;
    }

    public Hit<DataCatalogModel> searchId(String id)
            throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[searchId] start");
        try {
            return client.search(s -> s.index(openSearchConfig.getDataSetIndex())
                            .query(q -> q.match(
                                    m -> m.field("id").query(FieldValue.of(id))
                            ))
                    , DataCatalogModel.class).hits().hits().get(0);
        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public Hit<RecentSearchesModel> searchRecent(String userId) throws OpenSearchException, IOException {
        log.info("[searchRecent] start");
        try {
            var hits = client.search(s -> s.index(openSearchConfig.getRecentSearchesIndex())
                            .query(q -> q.match(
                                    m -> m.field("userId").query(FieldValue.of(userId))
                            )),
                    RecentSearchesModel.class).hits().hits();

            return !hits.isEmpty() ? hits.get(0) : null;
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }


    public String[] getResentSearches(String userId)
            throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[getResentSearches] start");
        try {
            return client.search(s -> s.index(openSearchConfig.getRecentSearchesIndex())
                            .size(openSearchConfig.getNumberOfRecentSearches())
                            .query(q -> q.match(
                                    m -> m.field("userId").query(FieldValue.of(userId))
                            )),
                    RecentSearchesModel.class).hits().hits().get(0).source().getRecentSearches();
        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void createIndex() throws OpenSearchException, IOException {
        try {
            log.info("[createIndex] Create data model index");
            if (checkIndex(openSearchConfig.getDataSetIndex())) {
                var properties = new HashMap<String, Property>();
                // todo properties -> meta는 필요 없음?
                for (var field : DataCatalogModel.class.getDeclaredFields()) {
                    if (field.getType() == String.class) {
                        properties.put(field.getName(), new Property.Builder()
                                .text(new TextProperty.Builder().fielddata(true).build()).build());
                    }
                }

                var mappings = new TypeMapping.Builder().properties(properties).build();

                client.indices().create(c -> c.index(openSearchConfig.getDataSetIndex()).mappings(mappings));
            }

            if (checkIndex(openSearchConfig.getStorageIndex())) {
                client.indices().create(c -> c.index(openSearchConfig.getStorageIndex()));
            }

            if (checkIndex(openSearchConfig.getRecentSearchesIndex())) {
                client.indices().create(c -> c.index(openSearchConfig.getRecentSearchesIndex()));
            }

        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }

        try {
            log.info("[createIndex] Create recent searches index");
            if (checkIndex(openSearchConfig.getRecentSearchesIndex()))
                client.indices().create(c -> c.index(openSearchConfig.getRecentSearchesIndex()));
        } catch (OpenSearchException | IOException e) {
            log.error("[createIndex] Create recent search index Error, cause : {}, message : {}",
                    e.getCause(), e.getMessage());
        }
    }

    public boolean checkIndex(String indexName) throws IOException {
        try {
            return !client.indices().exists(e -> e.index(indexName)).value();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }


    public void insertDocument(DataCatalogModel dataCatalogModel) throws OpenSearchException, IOException {
        log.info("[insertDocument] start");
        try {
            var response = client.index(i -> i.index(openSearchConfig.getDataSetIndex())
                    .document(dataCatalogModel));
            if (response.result() != Result.Created) {
                log.error(String.format("Client Create Fail, result %s", response.result()));
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("OpenSearch Insert Fail")
                                        .type("INSERT")).status(500).build());
            }
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void insertDocument(RecentSearchesModel recentSearchesModel) throws OpenSearchException, IOException {
        try {
            var response = client.index(i -> i.index(openSearchConfig.getRecentSearchesIndex())
                    .document(recentSearchesModel));
            if (response.result() != Result.Created) {
                log.error(String.format("Client Create Fail, result %s", response.result()));
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("OpenSearch Insert Fail")
                                        .type("INSERT")).status(500).build());
            }
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * @param dataCatalogModel update document
     * @param id           document's id
     */
    public void updateDocument(DataCatalogModel dataCatalogModel, String id) throws OpenSearchException, IOException {
        log.info("[updateDocument] start");
        try {
            var docId = getDocumentId(id);

            var response = client.update(u -> u.index(openSearchConfig.getDataSetIndex())
                            .id(docId)
                            .doc(dataCatalogModel)
                    , DataCatalogModel.class);
            if (response.result() != Result.Updated && response.result() != Result.NoOp) {
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("Client Update fail, because of same document update twice")
                                        .type("UPDATE")).status(500).build());
            }
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * @param recentSearchesModel update document
     * @param docId               document's id
     */
    public void updateDocument(RecentSearchesModel recentSearchesModel, String docId) throws OpenSearchException, IOException {
        try {
            var response = client.update(u -> u.index(openSearchConfig.getRecentSearchesIndex())
                            .id(docId)
                            .doc(recentSearchesModel)
                    , RecentSearchesModel.class);
            if (response.result() != Result.Updated && response.result() != Result.NoOp) {
                log.error(String.format("Client Update Fail, result %s", response.result()));
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("Client Update fail, because of same document update twice")
                                        .type("UPDATE")).status(500).build());
            }
        } catch (OpenSearchException | IOException e) {
            log.error("[updateDocument] cause : {}, message : {}", e.getCause(), e.getMessage());
            throw e;
        }
    }

    public void deleteDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteDocument] start");
        try {
            var docId = getDocumentId(id);
            var response = client.delete(d -> d.index(openSearchConfig.getDataSetIndex()).id(docId));
            if (response.result() != Result.Deleted) {
                log.error(String.format("Client Delete Fail, result %s", response.result()));
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("OpenSearch Delete Fail")
                                        .type("DELETE")).status(500).build());
            }
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void deleteSearchesDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteSearchesDocument] start");
        try {
            var docId = getRecentDocumentId(id);
            var response = client.delete(d -> d.index(openSearchConfig.getRecentSearchesIndex()).id(docId));
            if (response.result() != Result.Deleted) {
                log.error(String.format("Client Delete Fail, result %s", response.result()));
                throw new OpenSearchException(
                        new ErrorResponse.Builder().error(
                                e -> e.reason("OpenSearch Delete Fail")
                                        .type("DELETE")).status(500).build());
            }
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public String getDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
        try {
            return client.search(s -> s.index(openSearchConfig.getDataSetIndex())
                    .query(q -> q.match(
                            m -> m.field("id").query(FieldValue.of(id))
                    )), DataCatalogModel.class).hits().hits().get(0).id();
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public String getRecentDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
        try {
            return client.search(s -> s.index(openSearchConfig.getRecentSearchesIndex())
                    .query(q -> q.match(
                            m -> m.field("userId").query(FieldValue.of(id))
                    )), RecentSearchesModel.class).hits().hits().get(0).id();
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}

