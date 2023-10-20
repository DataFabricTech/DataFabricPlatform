package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.model.OpenSearchModel;
import com.mobigen.datafabric.dataLayer.repository.MultiRepository;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.datafabric.dataLayer.repository.RDBMSRepository;
import com.mobigen.libs.grpc.Column;
import com.mobigen.libs.grpc.QueryResponseMessage;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;

public class RDBMSService {
    private final MultiRepository multiRepository;
    private final RDBMSRepository rdbmsRepository;
    private final DBConfig dbConfig;

    public RDBMSService(MultiRepository multiRepository, RDBMSRepository rdbmsRepository, DBConfig dbConfig) {
        this.rdbmsRepository = rdbmsRepository;
        this.multiRepository = multiRepository;
        this.dbConfig = dbConfig;
    }

    public QueryResponseMessage query(String query) {
        var dataModelTableName = dbConfig.getTableName();
        try {
            var statement = CCJSqlParserUtil.parse(query);
            switch (statement.getClass().getSimpleName()) {
                case "PlainSelect":
                    return rdbmsRepository.selectQuery(query);
                case "Insert":
                    var insertStatement = (Insert) statement;
                    if (insertStatement.getTable().getName().equals(dataModelTableName)) {
                        return multiRepository.insert(query);
                    } else {
                        return rdbmsRepository.execute(query);
                    }
                case "Delete":
                    Delete deleteStatement = (Delete) statement;
                    if (deleteStatement.getTable().getName().equals(dataModelTableName)) {
                        return multiRepository.delete(query);
                    } else {
                        return rdbmsRepository.execute(query);
                    }
                case "Update":
                    Update updateStatement = (Update) statement;
                    if (updateStatement.getTable().getName().equals(dataModelTableName)) {
                        return multiRepository.update(query);
                    } else {
                        return rdbmsRepository.execute(query);
                    }
                default:
                    return rdbmsRepository.execute(query);
            }
        } catch (JSQLParserException e) {
            // todo query parsing 실패 log
            e.printStackTrace();
        }
        return null;
    }

    public OpenSearchModel convertToOpenSearch(Insert insertStatement) {
        var columns = insertStatement.getColumns();
        var values = insertStatement.getValues();
        var hashMap = new HashMap<String, Object>();
        // TODO 여기 할 차례다!!!!!!!!!!!!!!!!!!!!!!!!!
        return null;
    }
}
