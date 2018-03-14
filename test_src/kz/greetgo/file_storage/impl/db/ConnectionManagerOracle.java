package kz.greetgo.file_storage.impl.db;

import kz.greetgo.conf.SysParams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

public class ConnectionManagerOracle extends ConnectionManager {

  @Override
  public Connection getNewConnection() throws Exception {
    Class.forName("oracle.jdbc.driver.OracleDriver");

    try {
      return DriverManager.getConnection(url(), mySchema(), mySchema());
    } catch (SQLException e) {
      prepareDbSchema();
      return DriverManager.getConnection(url(), mySchema(), mySchema());
    }
  }

  private String url() {
    return "jdbc:oracle:thin:@" + SysParams.oracleAdminHost() + ":" + SysParams.oracleAdminPort()
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

      query(connection, "create user " + mySchema() + " identified by " + mySchema());
      query(connection, "grant all privileges to " + mySchema());
    }
  }
}
