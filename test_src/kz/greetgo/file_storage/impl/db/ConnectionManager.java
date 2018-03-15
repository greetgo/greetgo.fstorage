package kz.greetgo.file_storage.impl.db;


import kz.greetgo.db.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public abstract class ConnectionManager {
  class DbConnector {
    public final String url, username, password;

    DbConnector(String url, String username, String password) {
      this.url = url;
      this.username = username;
      this.password = password;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DbConnector dbConnector = (DbConnector) o;
      return Objects.equals(url, dbConnector.url) &&
        Objects.equals(username, dbConnector.username) &&
        Objects.equals(password, dbConnector.password);
    }

    @Override
    public int hashCode() {
      return Objects.hash(url, username, password);
    }
  }

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
