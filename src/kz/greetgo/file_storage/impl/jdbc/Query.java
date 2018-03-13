package kz.greetgo.file_storage.impl.jdbc;

import kz.greetgo.file_storage.impl.logging.FileStorageLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Query implements AutoCloseable {

  private final Connection connection;

  public Query(Connection connection) {
    this.connection = connection;
  }

  public String sql;
  public List<Object> params = new ArrayList<>();

  private ResultSet rs = null;

  public ResultSet rs() throws SQLException {
    if (rs != null) return rs;
    prepareStatement();
    if (updated) throw new IllegalStateException("Please create new Query");

    try {
      rs = ps.executeQuery();
      queryExecutedAt = System.nanoTime();
      return rs;
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorExecuteQuery(sql, params, e, startedAt, preparedAt, System.nanoTime());
      throw e;
    }
  }

  private PreparedStatement ps = null;

  Long startedAt = null;
  Long preparedAt = null;
  Long queryExecutedAt = null;

  private void prepareStatement() throws SQLException {
    if (ps != null) return;
    startedAt = System.nanoTime();

    try {
      ps = connection.prepareStatement(sql);
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorPrepareStatement(sql, e, startedAt, System.nanoTime());
      throw e;
    }

    preparedAt = System.nanoTime();

    int index = 1;

    try {

      for (Object param : params) {

        ps.setObject(index, param);

        index++;
      }

    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorSetParameter(sql, params, index, e, startedAt, preparedAt, System.nanoTime());
      throw e;
    }

  }

  private boolean updated = false;
  private int updateCount = 0;

  public int update() throws SQLException {
    if (rs != null) throw new IllegalStateException("Please create new Query");
    updated = true;
    prepareStatement();
    try {
      return updateCount = ps.executeUpdate();
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorExecuteUpdate(sql, params, e, startedAt, preparedAt, System.nanoTime());
      throw e;
    }
  }

  @Override
  public void close() throws SQLException {

    try {

      if (rs != null) try {
        rs.close();
      } catch (SQLException | RuntimeException e) {
        if (startedAt != null) {
          FileStorageLogger.errorCloseResultSet(sql, params, e, startedAt, preparedAt, System.nanoTime());
        }
        throw e;
      }

    } finally {

      if (ps != null) try {
        ps.close();
      } catch (SQLException | RuntimeException e) {
        if (startedAt != null) {
          if (queryExecutedAt != null) {
            FileStorageLogger.errorClosePreparedStatementWithResultSet(sql, params, e,
              startedAt, preparedAt, queryExecutedAt, System.nanoTime());
          } else {
            FileStorageLogger.errorClosePreparedStatementOnUpdate(sql, params, e,
              startedAt, preparedAt, System.nanoTime());
          }
        }

        //noinspection ThrowFromFinallyBlock
        throw e;
      }
    }

    if (startedAt != null && FileStorageLogger.isTraceEnabled()) {

      if (queryExecutedAt == null) {

        FileStorageLogger.traceExecuteUpdate(sql, params, updateCount, startedAt, preparedAt, System.nanoTime());

      } else {

        FileStorageLogger.traceSelect(sql, params, startedAt, preparedAt, queryExecutedAt, System.nanoTime());

      }

    }

  }

  public void exec(String sql) throws SQLException {
    if (ps != null) throw new IllegalStateException("Please create new Query");
    long startedAt = System.nanoTime();
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
      if (FileStorageLogger.isTraceEnabled()) {
        FileStorageLogger.traceSimpleSql(sql, startedAt, System.nanoTime());
      }
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorSimpleSql(sql, e, startedAt, System.nanoTime());
      throw e;
    }
  }
}
