package kz.greetgo.file_storage.impl.db;

import kz.greetgo.conf.SysParams;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManagerOracle extends ConnectionManager {

  private static final ConcurrentHashMap<DbConnector, DataSource> dataSourceMap = new ConcurrentHashMap<>();

  private static final AtomicInteger nextId = new AtomicInteger(1);

  private DataSource getDataSource(String url, String username, String password) {
    return dataSourceMap.computeIfAbsent(new DbConnector(url, username, password), c -> {

      try {
        Class.forName("oracle.jdbc.pool.OracleDataSource");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      try {
        DriverManager.getConnection(c.url, c.username, c.password).close();
      } catch (SQLException e) {
        prepareDbSchema();
      }

      try {
        final PoolDataSource x = PoolDataSourceFactory.getPoolDataSource();

        x.setURL(c.url);
        x.setUser(c.username);
        x.setPassword(c.password);

        x.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        x.setConnectionPoolName("FS_223344_" + nextId.getAndIncrement());
        x.setMinPoolSize(1);
        x.setMaxPoolSize(20);
        x.setInitialPoolSize(1);
        x.setInactiveConnectionTimeout(120);
        x.setValidateConnectionOnBorrow(true);
        x.setMaxStatements(10);

        return x;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public Connection getNewConnection() throws Exception {
    return getDataSource(url(), mySchema(), mySchema()).getConnection();
  }

  private String url() {
    return "jdbc:oracle:thin:@" + SysParams.oracleAdminHost()
      + ":" + SysParams.oracleAdminPort()
      + ":" + SysParams.oracleAdminSid();
  }

  private void prepareDbSchema() {
    try {
      prepareDbSchemaException();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String mySchema() {
    return System.getProperty("user.name") + '_' + getDbSchema();
  }

  private void prepareDbSchemaException() throws Exception {
    Class.forName("oracle.jdbc.driver.OracleDriver");

    try (Connection connection = DriverManager.getConnection(
      url(), SysParams.oracleAdminUserid(), SysParams.oracleAdminPassword()
    )) {

      try {
        query(connection, "alter session set \"_oracle_script\"=true");
      } catch (SQLSyntaxErrorException ignore) {}

      createUser(connection);
    }
  }

  private void createUser(Connection connection) throws SQLException {
    try {
      query(connection, "create user " + mySchema() + " identified by " + mySchema());
      query(connection, "grant all privileges to " + mySchema());
    } catch (SQLException e) {
      if (e.getMessage().startsWith("ORA-01920:")) return;
      throw e;
    }
  }
}
