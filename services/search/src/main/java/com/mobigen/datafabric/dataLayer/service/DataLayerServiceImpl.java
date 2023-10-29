package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

@Slf4j
public class DataLayerServiceImpl implements DataLayerCallBack {
    private final OpenSearchService openSearchService;
    private final RDBMSService rdbmsService;
    private final DBConfig dbConfig;

    public DataLayerServiceImpl(OpenSearchService openSearchService, RDBMSService rdbmsService, DBConfig dbConfig) {
        this.openSearchService = openSearchService;
        this.rdbmsService = rdbmsService;
        this.dbConfig = dbConfig;
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
    public SearchResponse search(String input, DataModel detailSearch, Filter filterSearch, String userId) {
        var searchResponse = SearchResponse.newBuilder();
        try {
            // todo 여기에 filter관련 정보를 안줬네

            var searchResponseBuilder = SearchResponse.newBuilder();
            var dataModelIds = openSearchService.search(input, detailSearch, filterSearch, userId);
            var storageIds = openSearchService.search(input);
            // dataModelIds와 stroageIds를 가져왔다.
            var sql = "select * from %s where id = '%s'";
            for (var dataModelId : dataModelIds) {
                searchResponseBuilder
                        .addDataSet(
                                rdbmsService.executeQuery(String.format(sql, dbConfig.getDataSet(), dataModelId)));
            }

            for (var storageId: storageIds) {
                searchResponseBuilder
                        .addStorage(
                                rdbmsService.executeQuery(String.format(sql, dbConfig.getStorage(), storageId)));
            }
            // TODO! !!!!!!!!!!!!!!!!!!!!!  여기에 table을 분석해서 filter 조건을 넣는 방법을 생성
            var dataSetTable = rdbmsService.executeQuery(String.format(sql, dbConfig.getDataSet()));
//            var rdbmsService.executeQuery(String.format(sql, dbConfig.getStorage()));
        } catch (ClassNotFoundException | SQLException | OpenSearchException | IOException e) {
            return searchResponse.setErrorMessage(e.getMessage()).setStatue(false).build();
        }
        return SearchResponse.newBuilder().build();
    }

    @Override
    public RecentSearchesResponse recentSearch(String userId) {
/*
        TODO List;
        1. config에서 개수 만큼만 가져오게 config 설정할 필요가 있다.
        2. 시간을 넘었을 시 삭제하는 로직 필요 -> 우선 순위 낮음
*/
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
}
