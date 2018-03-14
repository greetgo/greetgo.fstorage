package kz.greetgo.file_storage.impl.util;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.impl.db.ConnectionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TestUtil {
  public static DataSource createFrom(DbType dbType, String schemaSuffix) {
    ConnectionManager connectionManager = ConnectionManager.get(dbType);
    connectionManager.setDbSchema(schemaSuffix);
    return new AbstractDataSource() {
      @Override
      public Connection getConnection() throws SQLException {
        try {
          return connectionManager.getNewConnection();
        } catch (Exception e) {
          if (e instanceof SQLException) throw (SQLException) e;
          throw new RuntimeException(e);
        }
      }
    };
  }
}
