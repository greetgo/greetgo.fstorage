package kz.greetgo.file_storage.impl;

import kz.greetgo.test.db_providers.connections.ConnectionManager;
import kz.greetgo.util.db.AbstractDataSource;
import kz.greetgo.util.db.DbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TestUtil {
  public static DataSource createFrom(DbType dbType, String schemaSuffix) {
    ConnectionManager connectionManager = ConnectionManager.get(dbType);
    connectionManager.setDbSchema(schemaSuffix);

    return new AbstractDataSource() {
      @Override
      public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
      }

      @Override
      public Connection getConnection() throws SQLException {
        try {
          return connectionManager.getNewConnection();
        } catch (Exception e) {
          if (e instanceof SQLException) throw (SQLException) e;
          throw new RuntimeException(e);
        }
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException();
      }
    };
  }
}
