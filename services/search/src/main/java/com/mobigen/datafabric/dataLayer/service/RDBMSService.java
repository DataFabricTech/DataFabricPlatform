package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.repository.RDBMSRepository;
import com.mobigen.libs.grpc.QueryResponse;
import com.mobigen.libs.grpc.Table;

import java.sql.Connection;
import java.sql.SQLException;

public class RDBMSService {

    private final RDBMSRepository rdbmsRepository;

    public RDBMSService(RDBMSRepository rdbmsRepository) {
        this.rdbmsRepository = rdbmsRepository;
    }

    public Table executeQuery(String sql) throws SQLException, ClassNotFoundException {
        return rdbmsRepository.executeQuery(sql);
    }

    public void executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        rdbmsRepository.executeUpdate(sql);
    }

    public void executeBatchUpdate(String[] sqls) throws SQLException, ClassNotFoundException {
        rdbmsRepository.executeBatchUpdate(sqls);
    }
}
