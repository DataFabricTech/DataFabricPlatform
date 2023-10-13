package com.mobigen.datafabric.core.connector;

import java.sql.SQLException;

public interface ConnectorInterface extends AutoCloseable {
    void connect() throws SQLException;
    void execute(String sql);
    // fetchAll
    // fetchMany
    // fetchOne
}
