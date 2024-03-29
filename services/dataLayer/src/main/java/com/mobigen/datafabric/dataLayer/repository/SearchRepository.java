package com.mobigen.datafabric.dataLayer.repository;

//@Slf4j
//public class SearchRepository {
//    private final OpenSearchClient client;
////    private final SearchConfig searchConfig;
//    private SearchConfig searchConfig;
//
//    public SearchRepository() {
////        this.searchConfig = new SearchConfig(new Config().getConfig());
//        this.client = getClient();
//    }
//
//    private OpenSearchClient getClient() throws OpenSearchException, NullPointerException {
//        try {
//            log.info("OpenSearch Server: {}:{}", searchConfig.getHost(), searchConfig.getPort());
//
//            final HttpHost[] hosts = new HttpHost[]{
//                    new HttpHost("http", searchConfig.getHost(), searchConfig.getPort())
//            };
//
//            final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
//                    .builder(hosts)
//                    .setMapper(new JacksonJsonpMapper())
//                    .build();
//            return new OpenSearchClient(transport);
//        } catch (OpenSearchException e) {
//            log.error("[getClient] cause : {}, message : {}", e.getCause(), e.getMessage());
//            throw e;
//        } catch (NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void createIndex() throws OpenSearchException, IOException {
//        try {
//            log.info("[createIndex] Create data model index");
//            if (checkIndex(searchConfig.getDataModelIndex())) {
//                var properties = new HashMap<String, Property>();
//                // todo properties -> meta는 필요 없음?
//                for (var field : DataModel.class.getDeclaredFields()) {
//                    if (field.getType() == String.class) {
//                        properties.put(field.getName(), new Property.Builder()
//                                .text(new TextProperty.Builder().fielddata(true).build()).build());
//                    }
//                }
//                var mappings = new TypeMapping.Builder().properties(properties).build();
//
//                client.indices().create(c -> c.index(searchConfig.getDataModelIndex()).mappings(mappings));
//            }
//
//            if (checkIndex(searchConfig.getStorageIndex())) {
//                client.indices().create(c -> c.index(searchConfig.getStorageIndex()));
//            }
//
//            if (checkIndex(searchConfig.getRecentSearchesIndex())) {
//                client.indices().create(c -> c.index(searchConfig.getRecentSearchesIndex()));
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//
//        try {
//            log.info("[createIndex] Create recent searches index");
//            if (checkIndex(searchConfig.getRecentSearchesIndex()))
//                client.indices().create(c -> c.index(searchConfig.getRecentSearchesIndex()));
//        } catch (OpenSearchException | IOException e) {
//            log.error("[createIndex] Create recent search index Error, cause : {}, message : {}",
//                    e.getCause(), e.getMessage());
//        }
//    }
//
//    public boolean checkIndex(String indexName) throws IOException {
//        try {
//            return !client.indices().exists(e -> e.index(indexName)).value();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void insertDocument() {
//        try {
//            var model = Model.builder()
//                    .modelId(UUID.randomUUID())
//                    .name("name")
//                    .build();
//            var response = client.index(i -> i.index(searchConfig.getDataModelIndex())
//                    .document(model));
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//
//    public void insertDocument(DataModel dataModel) throws OpenSearchException, IOException {
//        log.info("[insertDocument] start");
//        try {
//            var response = client.index(i -> i.index(searchConfig.getDataModelIndex())
//                    .document(dataModel));
//            // todo meta inserted null value, check this
//
//            if (response.result() != Result.Created) {
//                log.error(String.format("Client Create Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Insert Fail")
//                                        .type("INSERT")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void insertDocument(StorageModel storageModel) throws OpenSearchException, IOException {
//        log.info("[insertDocument] start");
//        try {
//            var response = client.index(i -> i.index(searchConfig.getStorageIndex())
//                    .document(storageModel));
//
//            if (response.result() != Result.Created) {
//                log.error(String.format("Client Create Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Insert Fail")
//                                        .type("INSERT")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//
//    public void insertDocument(RecentSearchesModel recentSearchesModel) throws OpenSearchException, IOException {
//        try {
//            var response = client.index(i -> i.index(searchConfig.getRecentSearchesIndex())
//                    .document(recentSearchesModel));
//            if (response.result() != Result.Created) {
//                log.error(String.format("Client Create Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Insert Fail")
//                                        .type("INSERT")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void updateDocument(DataModel dataModel, String id) throws OpenSearchException, IOException {
//        log.info("[updateDocument] start");
//        try {
//            var docId = getDataModelDocumentId(id);
//
//            var response = client.update(u -> u.index(searchConfig.getDataModelIndex())
//                            .id(docId)
//                            .doc(dataModel)
//                    , DataModel.class);
//            if (response.result() != Result.Updated && response.result() != Result.NoOp) {
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("Client Update fail, because of same document update twice")
//                                        .type("UPDATE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void updateDocument(StorageModel storageModel, String id) throws OpenSearchException, IOException {
//        log.info("[updateDocument] start");
//        try {
//            var docId = getStorageDocumentId(id);
//
//            var response = client.update(u -> u.index(searchConfig.getStorageIndex())
//                            .id(docId)
//                            .doc(storageModel)
//                    , DataModel.class);
//            if (response.result() != Result.Updated && response.result() != Result.NoOp) {
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("Client Update fail, because of same document update twice")
//                                        .type("UPDATE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void updateDocument(RecentSearchesModel recentSearchesModel, String docId) throws OpenSearchException, IOException {
//        try {
//            var response = client.update(u -> u.index(searchConfig.getRecentSearchesIndex())
//                            .id(docId)
//                            .doc(recentSearchesModel)
//                    , RecentSearchesModel.class);
//            if (response.result() != Result.Updated && response.result() != Result.NoOp) {
//                log.error(String.format("Client Update Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("Client Update fail, because of same document update twice")
//                                        .type("UPDATE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException e) {
//            log.error("[updateDocument] cause : {}, message : {}", e.getCause(), e.getMessage());
//            throw e;
//        }
//    }
//
//    public void deleteDataModelDocument(String id) throws OpenSearchException, IOException {
//        log.info("[deleteDataModelDocument] start");
//        try {
//            var docId = getDataModelDocumentId(id);
//            var response = client.delete(d -> d.index(searchConfig.getDataModelIndex()).id(docId));
//            if (response.result() != Result.Deleted) {
//                log.error(String.format("Client Delete Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Delete Fail")
//                                        .type("DELETE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public void deleteStorageDocument(String id) throws OpenSearchException, IOException {
//        log.info("[deleteStorageDocument] start");
//        try {
//            var docId = getStorageDocumentId(id);
//            var response = client.delete(d -> d.index(searchConfig.getStorageIndex()).id(docId));
//            if (response.result() != Result.Deleted) {
//                log.error(String.format("Client Delete Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Delete Fail")
//                                        .type("DELETE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public LinkedList<String> searchDataModelAll() throws OpenSearchException, IOException {
//        log.info("[search] start");
//        var ids = new LinkedList<String>();
//        try {
//            var hits = client.search(s -> s.index(searchConfig.getDataModelIndex())
//                            .size(10000)
//                    , DataModel.class).hits().hits();
//
//            hits.forEach(hit -> {
//                ids.add(hit.source().getId());
//            });
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//        return ids;
//    }
//
//    public LinkedList<String> searchStorageAll() throws OpenSearchException, IOException {
//        log.info("[search] start");
//        var ids = new LinkedList<String>();
//        try {
//            var hits = client.search(s -> s.index(searchConfig.getStorageIndex())
//                            .size(10000)
//                    , StorageModel.class).hits().hits();
//
//            hits.forEach(hit -> {
//                ids.add(hit.source().getId());
//            });
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//        return ids;
//    }
//
//
//    public SearchModel search(List<Query> mainQuery, List<Query> mustQuery
//            , List<Query> shouldQuery, Utilities.Pageable pageable)
//            throws OpenSearchException, IOException {
//        log.info("[search] start");
//        var searchModel = new SearchModel();
//        try {
//            var sortOptions = getSortOptions(pageable);
//
//            var boolQueryBuilder = new BoolQuery.Builder();
//            if (mainQuery != null && !mainQuery.isEmpty()) boolQueryBuilder.should(mainQuery);
//            if (mustQuery != null && !mustQuery.isEmpty()) boolQueryBuilder.must(mustQuery);
//            if (shouldQuery != null && !shouldQuery.isEmpty())
//                boolQueryBuilder.should(shouldQuery).minimumShouldMatch("2");
//            else boolQueryBuilder.minimumShouldMatch("1");
//            var boolQuery = boolQueryBuilder.build();
//
//            // for page's total size
//            var hits = client.search(s -> s.index(searchConfig.getDataModelIndex())
//                            .size(searchConfig.getLimitSearchSize())
//                            .query(q -> q.bool(boolQuery))
//                    , DataModel.class).hits().hits();
//            searchModel.setTotalSize(hits.size());
//
//            hits = client.search(s -> s.index(searchConfig.getDataModelIndex())
//                            .size(pageable.getPage().getSize() == 0 ?
//                                    searchConfig.getDefaultSearchSize() :
//                                    pageable.getPage().getSize())
//                            .from(pageable.getPage().getSelectPage() <= 0 ?
//                                    0 :
//                                    (pageable.getPage().getSelectPage() - 1) * pageable.getPage().getSize())
//                            .sort(sortOptions)
//                            .query(q -> q.bool(boolQuery))
//                    , DataModel.class).hits().hits();
//
//
//            var dataModel = new LinkedList<DataModel>();
//            hits.forEach(hit -> {
//                dataModel.add(hit.source());
//            });
//            searchModel.setDataModelList(dataModel);
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//
//        return searchModel;
//    }
//
//    public SearchModel search(List<Query> storageQuery, Utilities.Pageable pageable)
//            throws IOException, OpenSearchException {
//        log.info("[search] start");
//        var searchModel = new SearchModel();
//        try {
//            var sortOptions = getSortOptions(pageable);
//
//            // for page's total size
//            var hits = client.search(s -> s.index(searchConfig.getStorageIndex())
//                            .size(searchConfig.getLimitSearchSize())
//                            .query(q -> q.bool(b -> b.minimumShouldMatch("1").should(storageQuery)))
//                    , StorageModel.class).hits().hits();
//            searchModel.setTotalSize(hits.size());
//
//            hits = client.search(s -> s.index(searchConfig.getStorageIndex())
//                            .size(pageable.getPage().getSize() == 0 ?
//                                    searchConfig.getDefaultSearchSize() :
//                                    pageable.getPage().getSize())
//                            .from(pageable.getPage().getSelectPage() <= 0 ?
//                                    0 :
//                                    (pageable.getPage().getSelectPage() - 1) * pageable.getPage().getSize())
//                            .sort(sortOptions)
//                            .query(q -> q.bool(b -> b.minimumShouldMatch("1").should(storageQuery)))
//                    , StorageModel.class).hits().hits();
//
//            var storageModels = new LinkedList<StorageModel>();
//            hits.forEach(hit -> {
//                storageModels.add(hit.source());
//            });
//            searchModel.setStorageModelList(storageModels);
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//        return searchModel;
//    }
//
//    public Map<String, Aggregate> getFacet(List<Query> mainQuery, List<Query> mustQuery, List<Query> shouldQuery)
//            throws OpenSearchException, IOException {
//        log.info("[getFacet] start");
//        try {
//            var map = new HashMap<String, Aggregation>();
//            for (var field : DataModel.class.getDeclaredFields()) {
//                if (field.getType() == String.class) {
//                    map.put(field.getName(),
//                            new Aggregation.Builder().terms(t -> t.field(field.getName())).build());
//                }
//            }
//
//            var boolQuery = new BoolQuery.Builder();
//            if (mainQuery != null && !mainQuery.isEmpty()) boolQuery.should(mainQuery);
//            if (mustQuery != null && !mustQuery.isEmpty()) boolQuery.must(mustQuery);
//            if (shouldQuery != null && !shouldQuery.isEmpty()) boolQuery.should(shouldQuery).minimumShouldMatch("2");
//            else boolQuery.minimumShouldMatch("1");
//
//            return client.search(s -> s.index(searchConfig.getDataModelIndex()).size(1000)
//                            .aggregations(map)
//                            .query(q -> q.bool(boolQuery.build()))
//                    , DataModel.class).aggregations();
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public String getDataModelDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
//        try {
//            return client.search(s -> s.index(searchConfig.getDataModelIndex())
//                    .query(q -> q.match(
//                            m -> m.field("id").query(FieldValue.of(id))
//                    )), DataModel.class).hits().hits().get(0).id();
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public String getStorageDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
//        try {
//            return client.search(s -> s.index(searchConfig.getStorageIndex())
//                    .query(q -> q.match(
//                            m -> m.field("id").query(FieldValue.of(id.substring(1, id.length() - 1)))
//                    )), StorageModel.class).hits().hits().get(0).id();
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public DataModel getDataModelDocument(String id)
//            throws OpenSearchException, IOException, IndexOutOfBoundsException {
//        log.info("[searchId] start");
//        try {
//            return client.search(s -> s.index(searchConfig.getDataModelIndex())
//                            .query(q -> q.match(
//                                    m -> m.field("id").query(FieldValue.of(id))
//                            ))
//                    , DataModel.class).hits().hits().get(0).source();
//        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public StorageModel getStorageDocuemnt(String id)
//            throws OpenSearchException, IOException, IndexOutOfBoundsException {
//        log.info("[searchId] start");
//        try {
//            return client.search(s -> s.index(searchConfig.getStorageIndex())
//                            .query(q -> q.match(
//                                    m -> m.field("id").query(FieldValue.of(id))
//                            ))
//                    , StorageModel.class).hits().hits().get(0).source();
//        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    public Hit<RecentSearchesModel> recentSearches(String userId) throws OpenSearchException, IOException {
//        log.info("[recentSearches] start");
//        try {
//            var hits = client.search(s -> s.index(searchConfig.getRecentSearchesIndex())
//                            .query(q -> q.match(
//                                    m -> m.field("userId").query(FieldValue.of(userId))
//                            )),
//                    RecentSearchesModel.class).hits().hits();
//
//            return !hits.isEmpty() ? hits.get(0) : null;
//        } catch (OpenSearchException | IOException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    private List<SortOptions> getSortOptions(Utilities.Pageable pageable) {
//        var sortPriorityQueue = new PriorityQueue<Utilities.Sort>(Comparator.comparingInt(Utilities.Sort::getOrder));
//        sortPriorityQueue.addAll(pageable.getSortList());
//        List<SortOptions> sortOptions = new ArrayList<>();
//
//        while (!sortPriorityQueue.isEmpty()) {
//            var sort = sortPriorityQueue.poll();
//            sortOptions.add(
//                    new SortOptions.Builder().field(
//                            f -> f.field(sort.getField())
//                                    .order(sort.getDirectionValue() == 0 ?
//                                            SortOrder.Asc :
//                                            SortOrder.Desc)).build());
//        }
//
//        return sortOptions;
//    }
//
//    public void deleteSearchesDocument(String id) throws OpenSearchException, IOException {
//        log.info("[deleteSearchesDocument] start");
//        try {
//            var docId = getRecentDocumentId(id);
//            var response = client.delete(d -> d.index(searchConfig.getRecentSearchesIndex()).id(docId));
//            if (response.result() != Result.Deleted) {
//                log.error(String.format("Client Delete Fail, result %s", response.result()));
//                throw new OpenSearchException(
//                        new ErrorResponse.Builder().error(
//                                e -> e.reason("OpenSearch Delete Fail")
//                                        .type("DELETE")).status(500).build());
//            }
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//
//    private String getRecentDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
//        try {
//            return client.search(s -> s.index(searchConfig.getRecentSearchesIndex())
//                    .query(q -> q.match(
//                            m -> m.field("userId").query(FieldValue.of(id))
//                    )), RecentSearchesModel.class).hits().hits().get(0).id();
//        } catch (OpenSearchException | IOException | NullPointerException e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//    }
//}