package kz.greetgo.file_storage.impl.db;

import kz.greetgo.conf.SysParams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionManagerPostgreSQL extends ConnectionManager {
  
  @Override
  public Connection getNewConnection() throws Exception {
    Class.forName("org.postgresql.Driver");
    
    try {
      return DriverManager.getConnection(getConnectionUrl(), getDbSchema(), getDbSchema());
    } catch (SQLException e) {
      prepareDbSchema();
      
      return DriverManager.getConnection(getConnectionUrl(), getDbSchema(), getDbSchema());
    }
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

      query(con, "create user " + getDbSchema() + " with password '" + getDbSchema() + "'");
      query(con, "create database " + getDbSchema() + " with owner " + getDbSchema());

    }
  }
}
