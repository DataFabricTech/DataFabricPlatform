package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.config.PortalConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.SearchModel;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
import com.mobigen.datafabric.share.protobuf.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.*;
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
import java.util.*;

@Slf4j
public class PortalRepository {
    private final OpenSearchClient client;
    private final PortalConfig portalConfig;

    public PortalRepository(PortalConfig portalConfig) {
        this.portalConfig = portalConfig;
        this.client = getClient();
    }

    private OpenSearchClient getClient() throws OpenSearchException, NullPointerException {
        try {
            log.info("OpenSearch Server: {}:{}", portalConfig.getHost(), portalConfig.getPort());

            final HttpHost[] hosts = new HttpHost[]{
                    new HttpHost("http", portalConfig.getHost(), portalConfig.getPort())
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

    public void createIndex() throws OpenSearchException, IOException {
        try {
            log.info("[createIndex] Create data model index");
            if (checkIndex(portalConfig.getDataCatalogIndex())) {
                var properties = new HashMap<String, Property>();
                // todo properties -> meta는 필요 없음?
                for (var field : DataCatalogModel.class.getDeclaredFields()) {
                    if (field.getType() == String.class) {
                        properties.put(field.getName(), new Property.Builder()
                                .text(new TextProperty.Builder().fielddata(true).build()).build());
                    }
                }
                var mappings = new TypeMapping.Builder().properties(properties).build();

                client.indices().create(c -> c.index(portalConfig.getDataCatalogIndex()).mappings(mappings));
            }

            if (checkIndex(portalConfig.getStorageIndex())) {
                client.indices().create(c -> c.index(portalConfig.getStorageIndex()));
            }

            if (checkIndex(portalConfig.getRecentSearchesIndex())) {
                client.indices().create(c -> c.index(portalConfig.getRecentSearchesIndex()));
            }

        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }

        try {
            log.info("[createIndex] Create recent searches index");
            if (checkIndex(portalConfig.getRecentSearchesIndex()))
                client.indices().create(c -> c.index(portalConfig.getRecentSearchesIndex()));
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
            var response = client.index(i -> i.index(portalConfig.getDataCatalogIndex())
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

    public void insertDocument(StorageModel storageModel) throws OpenSearchException, IOException {
        log.info("[insertDocument] start");
        try {
            var response = client.index(i -> i.index(portalConfig.getStorageIndex())
                    .document(storageModel));

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
            var response = client.index(i -> i.index(portalConfig.getRecentSearchesIndex())
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

    public void updateDocument(DataCatalogModel dataCatalogModel, String id) throws OpenSearchException, IOException {
        log.info("[updateDocument] start");
        try {
            var docId = getDataCatalogDocumentId(id);

            var response = client.update(u -> u.index(portalConfig.getDataCatalogIndex())
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

    public void updateDocument(StorageModel storageModel, String id) throws OpenSearchException, IOException {
        log.info("[updateDocument] start");
        try {
            var docId = getStorageDocumentId(id);

            var response = client.update(u -> u.index(portalConfig.getStorageIndex())
                            .id(docId)
                            .doc(storageModel)
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

    public void updateDocument(RecentSearchesModel recentSearchesModel, String docId) throws OpenSearchException, IOException {
        try {
            var response = client.update(u -> u.index(portalConfig.getRecentSearchesIndex())
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

    public void deleteDataCatalogDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteDataCatalogDocument] start");
        try {
            var docId = getDataCatalogDocumentId(id);
            var response = client.delete(d -> d.index(portalConfig.getDataCatalogIndex()).id(docId));
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

    public void deleteStorageDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteStorageDocument] start");
        try {
            var docId = getStorageDocumentId(id);
            var response = client.delete(d -> d.index(portalConfig.getStorageIndex()).id(docId));
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

    public LinkedList<String> searchDataCatalogAll() throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            var hits = client.search(s -> s.index(portalConfig.getDataCatalogIndex())
                            .size(10000)
                    , DataCatalogModel.class).hits().hits();

            hits.forEach(hit -> {
                ids.add(hit.source().getId());
            });
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return ids;
    }

    public LinkedList<String> searchStorageAll() throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            // todo size -> config
            var hits = client.search(s -> s.index(portalConfig.getStorageIndex())
                            .size(10000)
                    , StorageModel.class).hits().hits();

            hits.forEach(hit -> {
                ids.add(hit.source().getId());
            });
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return ids;
    }


    public SearchModel search(List<Query> mainQuery, List<Query> mustQuery
            , List<Query> shouldQuery, Utilities.Pageable pageable)
            throws OpenSearchException, IOException {
        log.info("[search] start");
        var searchModel = new SearchModel();
        try {
            var sortOptions = getSortOptions(pageable);

            var boolQueryBuilder = new BoolQuery.Builder();
            if (mainQuery != null && !mainQuery.isEmpty()) boolQueryBuilder.should(mainQuery);
            if (mustQuery != null && !mustQuery.isEmpty()) boolQueryBuilder.must(mustQuery);
            if (shouldQuery != null && !shouldQuery.isEmpty()) boolQueryBuilder.should(shouldQuery).minimumShouldMatch("2");
            else boolQueryBuilder.minimumShouldMatch("1");
            var boolQuery = boolQueryBuilder.build();

            // for page's total size
            var hits = client.search(s -> s.index(portalConfig.getDataCatalogIndex())
                            .size(portalConfig.getLimitSearchSize())
                            .query(q -> q.bool(boolQuery))
                    , DataCatalogModel.class).hits().hits();
            searchModel.setTotalSize(hits.size());

            hits = client.search(s -> s.index(portalConfig.getDataCatalogIndex())
                            .size(pageable.getPage().getSize() == 0 ?
                                    portalConfig.getDefaultSearchSize() :
                                    pageable.getPage().getSize())
                            .from(pageable.getPage().getSelectPage() <= 0 ?
                                    0 :
                                    (pageable.getPage().getSelectPage() - 1) * pageable.getPage().getSize())
                            .sort(sortOptions)
                            .query(q -> q.bool(boolQuery))
                    , DataCatalogModel.class).hits().hits();


            var dataCatalogModels = new LinkedList<DataCatalogModel>();
            hits.forEach(hit -> {
                dataCatalogModels.add(hit.source());
            });
            searchModel.setDataCatalogModelList(dataCatalogModels);
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }

        return searchModel;
    }

    public SearchModel search(List<Query> storageQuery, Utilities.Pageable pageable)
            throws IOException, OpenSearchException {
        log.info("[search] start");
        var searchModel = new SearchModel();
        try {
            var sortOptions = getSortOptions(pageable);

            // for page's total size
            var hits = client.search(s -> s.index(portalConfig.getStorageIndex())
                            .size(portalConfig.getLimitSearchSize())
                            .query(q -> q.bool(b -> b.minimumShouldMatch("1").should(storageQuery)))
                    , StorageModel.class).hits().hits();
            searchModel.setTotalSize(hits.size());

            hits = client.search(s -> s.index(portalConfig.getStorageIndex())
                            .size(pageable.getPage().getSize() == 0 ?
                                    portalConfig.getDefaultSearchSize() :
                                    pageable.getPage().getSize())
                            .from(pageable.getPage().getSelectPage() <= 0 ?
                                    0 :
                                    (pageable.getPage().getSelectPage() - 1) * pageable.getPage().getSize())
                            .sort(sortOptions)
                            .query(q -> q.bool(b -> b.minimumShouldMatch("1").should(storageQuery)))
                    , StorageModel.class).hits().hits();

            var storageModels = new LinkedList<StorageModel>();
            hits.forEach(hit -> {
                storageModels.add(hit.source());
            });
            searchModel.setStorageModelList(storageModels);
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
        return searchModel;
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

            return client.search(s -> s.index(portalConfig.getDataCatalogIndex()).size(1000)
                            .aggregations(map)
                            .query(q -> q.bool(boolQuery.build()))
                    , DataCatalogModel.class).aggregations();
        } catch (OpenSearchException | IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public String getDataCatalogDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
        try {
            return client.search(s -> s.index(portalConfig.getDataCatalogIndex())
                    .query(q -> q.match(
                            m -> m.field("id").query(FieldValue.of(id))
                    )), DataCatalogModel.class).hits().hits().get(0).id();
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public String getStorageDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
        try {
            return client.search(s -> s.index(portalConfig.getStorageIndex())
                    .query(q -> q.match(
                            m -> m.field("id").query(FieldValue.of(id.substring(1, id.length()-1)))
                    )), StorageModel.class).hits().hits().get(0).id();
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public DataCatalogModel getDataCatalogDocuemnt(String id)
            throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[searchId] start");
        try {
            return client.search(s -> s.index(portalConfig.getDataCatalogIndex())
                            .query(q -> q.match(
                                    m -> m.field("id").query(FieldValue.of(id))
                            ))
                    , DataCatalogModel.class).hits().hits().get(0).source();
        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public StorageModel getStorageDocuemnt(String id)
            throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[searchId] start");
        try {
            return client.search(s -> s.index(portalConfig.getStorageIndex())
                            .query(q -> q.match(
                                    m -> m.field("id").query(FieldValue.of(id))
                            ))
                    , StorageModel.class).hits().hits().get(0).source();
        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
    public Hit<RecentSearchesModel> recentSearches(String userId) throws OpenSearchException, IOException {
        log.info("[recentSearches] start");
        try {
            var hits = client.search(s -> s.index(portalConfig.getRecentSearchesIndex())
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

    private List<SortOptions> getSortOptions(Utilities.Pageable pageable) {
        var sortPriorityQueue = new PriorityQueue<Utilities.Sort>(Comparator.comparingInt(Utilities.Sort::getOrder));
        sortPriorityQueue.addAll(pageable.getSortList());
        List<SortOptions> sortOptions = new ArrayList<>();

        while (!sortPriorityQueue.isEmpty()) {
            var sort = sortPriorityQueue.poll();
            sortOptions.add(
                    new SortOptions.Builder().field(
                            f -> f.field(sort.getField())
                                    .order(sort.getDirectionValue() == 0 ?
                                            SortOrder.Asc :
                                            SortOrder.Desc)).build());
        }

        return sortOptions;
    }

    public void deleteSearchesDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteSearchesDocument] start");
        try {
            var docId = getRecentDocumentId(id);
            var response = client.delete(d -> d.index(portalConfig.getRecentSearchesIndex()).id(docId));
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

    private String getRecentDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
        try {
            return client.search(s -> s.index(portalConfig.getRecentSearchesIndex())
                    .query(q -> q.match(
                            m -> m.field("userId").query(FieldValue.of(id))
                    )), RecentSearchesModel.class).hits().hits().get(0).id();
        } catch (OpenSearchException | IOException | NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

}
