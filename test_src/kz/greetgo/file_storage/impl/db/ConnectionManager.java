package kz.greetgo.file_storage.impl.db;


import kz.greetgo.db.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class ConnectionManager {
  private String dbSchema;

  public static ConnectionManager get(DbType dbType) {

    switch (dbType) {
      case Postgres:
        return new ConnectionManagerPostgreSQL();
      case Oracle:
        return new ConnectionManagerOracle();
      default:
        throw new RuntimeException("Cannot create ConnectionManager for " + dbType);
    }
  }

  protected static void query(Connection con, String sql) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.executeUpdate();
    }
  }

  public abstract Connection getNewConnection() throws Exception;

  public void setDbSchema(String dbSchema) {
    this.dbSchema = dbSchema;
  }

  public String getDbSchema() {
    return dbSchema;
  }
}
