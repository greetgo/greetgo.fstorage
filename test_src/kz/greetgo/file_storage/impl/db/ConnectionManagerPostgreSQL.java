package kz.greetgo.file_storage.impl.db;

import kz.greetgo.conf.SysParams;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionManagerPostgreSQL extends ConnectionManager {

  private static final ConcurrentHashMap<DbConnector, DataSource> dataSourceMap = new ConcurrentHashMap<>();

  private DataSource getDataSource(String url, String username, String password) {
    return dataSourceMap.computeIfAbsent(new DbConnector(url, username, password), c -> {

      try {
        Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      try {
        DriverManager.getConnection(c.url, c.username, c.password).close();
      } catch (SQLException e) {
        prepareDbSchema();
      }

      {
        BasicDataSource pool = new BasicDataSource();

        pool.setDriverClassName("org.postgresql.Driver");
        pool.setUrl(c.url);
        pool.setUsername(c.username);
        pool.setPassword(c.password);

        pool.setInitialSize(0);

        return pool;
      }
    });
  }

  @Override
  public Connection getNewConnection() throws Exception {
    return getDataSource(getConnectionUrl(), getDbSchema(), getDbSchema()).getConnection();
  }

  private void prepareDbSchema() {
    try {
      prepareDbSchemaException();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final Pattern LAST_LASH = Pattern.compile("(.*/)[^/]+");

  private String getConnectionUrl() {
    String url = SysParams.pgAdminUrl();
    Matcher m = LAST_LASH.matcher(url);
    if (!m.matches()) throw new RuntimeException("Left admin url = " + url);
    return m.group(1) + getDbSchema();
  }

  private void prepareDbSchemaException() throws Exception {
    Class.forName("org.postgresql.Driver");

    try (Connection con = DriverManager.getConnection(SysParams.pgAdminUrl(), SysParams.pgAdminUserid(),
      SysParams.pgAdminPassword())) {

      createDatabase(con);

    }
  }

  private void createDatabase(Connection con) {
    try {
      query(con, "create user " + getDbSchema() + " with password '" + getDbSchema() + "'");
      query(con, "create database " + getDbSchema() + " with owner " + getDbSchema());
    } catch (SQLException e) {
      if ("23505".equals(e.getSQLState())) return;
      throw new RuntimeException("e.getSQLState() = " + e.getSQLState() + " :: " + e.getMessage(), e);
    }
  }
}
