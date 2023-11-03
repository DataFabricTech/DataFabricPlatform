package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.share.protobuf.DataLayer.*;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DataLayerServiceImpl implements DataLayerCallBack {
    private final DataLayerRepository dataLayerRepository;
    private final PortalServiceImpl portalService;

    public DataLayerServiceImpl(DataLayerRepository dataLayerRepository, PortalServiceImpl portalService) {
        this.dataLayerRepository = dataLayerRepository;
        this.portalService = portalService;
    }

    @Override
    public ResExecute execute(ReqExecute reqExecute) {
        log.info("[execute] start");
        var resExecute = ResExecute.newBuilder();
        var sql = reqExecute.getSql();
        try {
            var statement = CCJSqlParserUtil.parse(sql);
            if (statement.getClass().getSimpleName().equals("PlainSelect")) {
                var table = dataLayerRepository.executeQuery(sql);
                ResExecute.Data.newBuilder().setTable(table).build();
                return resExecute.setCode("200")
                        .setData(
                                ResExecute.Data.newBuilder()
                                        .setTable(table)
                                        .build())
                        .build();
            } else {
                resExecute
                        .setData(
                                ResExecute.Data.newBuilder()
                                        .setResponse(dataLayerRepository.executeUpdate(sql))
                                        .build()
                        );

                portalService.executeUpdate(sql);
                return resExecute.setCode("200").build();
            }
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
            return resExecute.setCode("400").setErrMsg(e.getMessage()).build();
        } catch (SQLException | OpenSearchException | IOException | ClassNotFoundException
                 | IndexOutOfBoundsException | NullPointerException e) {
            return resExecute.setCode("404").setErrMsg(e.getMessage()).build();
        }
    }

    @Override
    public ResBatchExecute executeBatch(ReqBatchExecute reqBatchExecute) {
        log.info("[executeBatch] start");
        var sqls = reqBatchExecute.getSqlList().toArray(new String[0]);
        var resBacthExecute = ResBatchExecute.newBuilder();
        try {
            for (int i = 0; i < sqls.length; i++) {
                var statement = CCJSqlParserUtil.parse(sqls[i]);
                if (statement.getClass().getSimpleName().equals("PlainSelect")) {
                    log.error("Batch not accept select Query");
                    return ResBatchExecute.newBuilder()
                            .setCode("400")
                            .setErrMsg(String.format("Batch not use Select %s", sqls[i]))
                            .build();
                }
            }

            var data = Arrays.stream(dataLayerRepository.executeBatchUpdate(sqls)).boxed().collect(Collectors.toList());
            resBacthExecute.addAllData(data);

            portalService.executeBatchUpdate(sqls);
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
            return ResBatchExecute.newBuilder()
                    .setCode("400")
                    .setErrMsg(e.getMessage())
                    .build();
        } catch (ClassNotFoundException | SQLException | IOException |
                 NullPointerException | OpenSearchException | IndexOutOfBoundsException e) {
            return ResBatchExecute.newBuilder()
                    .setCode("404")
                    .setErrMsg(e.getMessage())
                    .build();
        }
        return resBacthExecute.setCode("200").build();
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
