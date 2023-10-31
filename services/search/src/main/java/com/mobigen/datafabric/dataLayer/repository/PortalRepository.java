package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.config.PortalConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

@Slf4j
public class PortalRepository {
    private final OpenSearchClient client;
    private final PortalConfig portalConfig;

    public PortalRepository(PortalConfig portalConfig) {
        this.portalConfig = portalConfig;
        this.client = getClient();
    }

//    public List<String> search()

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


    public void deleteDocument(String id) throws OpenSearchException, IOException {
        log.info("[deleteDocument] start");
        try {
            var docId = getDocumentId(id);
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

    public void updateDocument(DataCatalogModel dataCatalogModel, String id) throws OpenSearchException, IOException {
        log.info("[updateDocument] start");
        try {
            var docId = getDocumentId(id);

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

    public LinkedList<String> searchAll() throws OpenSearchException, IOException {
        log.info("[search] start");
        var ids = new LinkedList<String>();
        try {
            // todo size -> config
            var hits = client.search(s -> s.index(portalConfig.getDataCatalogIndex())
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

    public String getDocumentId(String id) throws OpenSearchException, IOException, NullPointerException {
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

    public DataCatalogModel getDocuemnt(String id)
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
}
