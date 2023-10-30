package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.config.OpenSearchConfig;
import com.mobigen.libs.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class DataLayerServiceImpl implements DataLayerCallBack {
    private final OpenSearchService openSearchService;
    private final RDBMSService rdbmsService;
    private final DBConfig dbConfig;
    private final OpenSearchConfig openSearchConfig;

    public DataLayerServiceImpl(OpenSearchService openSearchService, RDBMSService rdbmsService, DBConfig dbConfig, OpenSearchConfig openSearchConfig) {
        this.openSearchService = openSearchService;
        this.rdbmsService = rdbmsService;
        this.dbConfig = dbConfig;
        this.openSearchConfig = openSearchConfig;
    }

    @Override
    public QueryResponse execute(String sql) {
        log.info("[execute] start");
        var queryResponseBuilder = QueryResponse.newBuilder();
        try {
            var statement = CCJSqlParserUtil.parse(sql);
            if (statement.getClass().getSimpleName().equals("PlainSelect")) {
                return executeQuery(sql);
            } else {
                if (statement instanceof Insert insertStatement) {
                    var id = UUID.randomUUID().toString();
                    addIdToSql(insertStatement, id);
                    sql = insertStatement.toString();
                }

                rdbmsService.executeUpdate(sql);
                openSearchService.executeUpdate(sql);

                return queryResponseBuilder.setStatus(true).build();
            }
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
            return queryResponseBuilder
                    .setErrorMessage(e.getMessage())
                    .setStatus(false)
                    .build();
        } catch (SQLException | OpenSearchException | IOException | ClassNotFoundException
                 | IndexOutOfBoundsException | NullPointerException e) {
            return queryResponseBuilder
                    .setErrorMessage(e.getMessage())
                    .setStatus(false)
                    .build();
        }
    }

    @Override
    public BatchResponse executeBatch(String[] sqls) {
        log.info("[query] start");
        try {
            for (int i = 0; i < sqls.length; i++) {
                var statement = CCJSqlParserUtil.parse(sqls[i]);
                if (statement.getClass().getSimpleName().equals("PlainSelect")) {
                    log.error("Batch not accept select Query");
                    return BatchResponse.newBuilder()
                            .setStatus(false)
                            .setErrorMessage(String.format("Batch not use Select %s", sqls[i]))
                            .build();
                }

                if (statement instanceof Insert insertStatement) {
                    var id = UUID.randomUUID().toString();
                    addIdToSql(insertStatement, id);
                    sqls[i] = insertStatement.toString();
                }
            }

            rdbmsService.executeBatchUpdate(sqls);
            openSearchService.executeBatchUpdate(sqls);
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
            return BatchResponse.newBuilder()
                    .setStatus(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        } catch (ClassNotFoundException | IOException | SQLException |
                 NullPointerException | OpenSearchException | IndexOutOfBoundsException e) {
            return BatchResponse.newBuilder()
                    .setStatus(false)
                    .setErrorMessage(e.getMessage())
                    .build();
        }
        return BatchResponse.newBuilder().setStatus(true).build();
    }

    @Override
    public SearchResponse search(String input, DataSet detailSearch, Filter filterSearch, String userId) {
        var searchResponse = SearchResponse.newBuilder();
        try {
            var searchResponseBuilder = SearchResponse.newBuilder();
            var dataSetIds = openSearchService.search(input, detailSearch, filterSearch, userId);
            var storageIds = openSearchService.search(input);

            var sql = "select * from %s where id = '%s'";
            if (!dataSetIds.isEmpty()) {
                var tableBuilder = Table.newBuilder();

                var tempTable = rdbmsService.executeQuery(String.format(sql, dbConfig.getDataSet(), dataSetIds.get(0)));
                tableBuilder.setColumns(tempTable.getColumns());

                for (var dataSetId : dataSetIds) {
                    var table = rdbmsService.executeQuery(String.format(sql, dbConfig.getDataSet(), dataSetId));
                    tableBuilder.addRows(table.getRows(0));
                }

                searchResponseBuilder.addDataSet(tableBuilder.build());
            }

            if (!storageIds.isEmpty()) {
                var tableBuilder = Table.newBuilder();

                var tempTable = rdbmsService.executeQuery(String.format(sql, dbConfig.getDataSet(), storageIds.get(0)));
                tableBuilder.setColumns(tempTable.getColumns());

                for (var storageId : storageIds) {
                    var table = rdbmsService.executeQuery(String.format(sql, dbConfig.getStorage(), storageId));
                    tableBuilder.addRows(table.getRows(0));
                }

                searchResponseBuilder.addStorage(tableBuilder.build());
            }

            var aggregations = openSearchService.getFacet(input, detailSearch, filterSearch);

            // todo  creator에 관한 것 추가 하기
            searchResponseBuilder.setFilters(filterBuilder(aggregations));
            return searchResponseBuilder.build();
        } catch (ClassNotFoundException | SQLException | OpenSearchException | IOException e) {
            return searchResponse.setErrorMessage(e.getMessage()).setStatue(false).build();
        }
    }

    @Override
    public RecentSearchesResponse recentSearch(String userId) {
        // todo 시간에 따른 recentsearch제거 로직
        var searches = openSearchService.getRecentSearch(userId);
        if (searches != null) {
            return RecentSearchesResponse.newBuilder()
                    .addAllSearched(Arrays.asList(searches)).build();
        } else {
            return RecentSearchesResponse.newBuilder().build();
        }
    }

    @Override
    public HealthCheckResponse healthCheck() {
        return HealthCheckResponse.newBuilder().setStatus(true).build();
    }

    private QueryResponse executeQuery(String sql) throws SQLException, ClassNotFoundException {
        return QueryResponse.newBuilder()
                .setTable(rdbmsService.executeQuery(sql))
                .setStatus(true)
                .build();
    }

    private void addIdToSql(Insert state, String id) throws NullPointerException {
        try {
            state.getColumns().add(new Column("id"));
            ((ExpressionList<Expression>) state.getSelect().getValues().getExpressions()).add(new StringValue(id));
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private FiltersResponse filterBuilder(Map<String, Aggregate> aggregations) {
        var fields = openSearchConfig.getFilters();
        var filters = FiltersResponse.newBuilder();

        for (var field: fields) {
            if (aggregations.get(field) != null) {
                var bucket = aggregations.get(field)._get()._toAggregate().sterms().buckets().array();
                bucket.forEach(b -> {
                    var filterResponse = FilterResponse.newBuilder();
                    filterResponse.setCount(b.docCount());
                    filterResponse.setName(b.key());
                    switch (field) {
                        case "name":
                            filters.addNameFilter(filterResponse);
                            break;
                        case "type":
                            filters.addTypeFilter(filterResponse);
                            break;
                        case "format":
                            filters.addFormatFilter(filterResponse);
                            break;
                        case "knowledgeGraph":
                            filters.addKnowledgeGraphFilter(filterResponse);
                            break;
                        case "categories":
                            filters.addCategoryFilter(filterResponse);
                            break;
                        case "tag":
                            filters.addTagFilter(filterResponse);
                            break;
                        case "connectorType":
                            filters.addConnectorTypeFilter(filterResponse);
                            break;
                        case "connectorName":
                            filters.addConnectorNameFilter(filterResponse);
                            break;
//                        case "creator":
//                            break;
                    }
                });
            }
        }

        return filters.build();
    }

    private FilterResponse buildFilter(String target, Map<String, Aggregate> aggregations) {
        if (aggregations.get(target) == null)
            return FilterResponse.newBuilder().build();

        var bucket = aggregations.get(target)._get()._toAggregate().sterms().buckets().array();
        var filtersResponse = FiltersResponse.newBuilder();

        var builder = FilterResponse.newBuilder();
        bucket.forEach(b -> {
            builder.setCount(b.docCount());
            builder.setName(b.key());
        });
        return builder.build();
    }
}
