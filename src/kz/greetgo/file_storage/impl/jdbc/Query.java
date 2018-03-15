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

  public final StringBuilder sql = new StringBuilder();
  public List<Object> params = new ArrayList<>();

  private ResultSet rs = null;

  public Query go() throws SQLException {
    prepareStatement();
    if (updated) throw new IllegalStateException("Please create new Query");

    try {
      rs = ps.executeQuery();
      queryExecutedAt = System.nanoTime();
      return this;
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorExecuteQuery(sql.toString(), params, e, startedAt, preparedAt, System.nanoTime());
      throw e;
    }
  }

  public ResultSet rs() throws SQLException {
    if (rs == null) throw new RuntimeException("Please call go()");
    return rs;
  }

  private PreparedStatement ps = null;

  Long startedAt = null;
  Long preparedAt = null;
  Long queryExecutedAt = null;

  private void prepareStatement() throws SQLException {
    if (ps != null) return;
    startedAt = System.nanoTime();

    try {
      ps = connection.prepareStatement(sql.toString());
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorPrepareStatement(sql.toString(), e, startedAt, System.nanoTime());
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
      FileStorageLogger.errorSetParameter(sql.toString(), params, index, e, startedAt, preparedAt, System.nanoTime());
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
      FileStorageLogger.errorExecuteUpdate(sql.toString(), params, e, startedAt, preparedAt, System.nanoTime());
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
          FileStorageLogger.errorCloseResultSet(sql.toString(), params, e, startedAt, preparedAt, System.nanoTime());
        }
        throw e;
      }

    } finally {

      if (ps != null) try {
        ps.close();
      } catch (SQLException | RuntimeException e) {
        if (startedAt != null) {
          if (queryExecutedAt != null) {
            FileStorageLogger.errorClosePreparedStatementWithResultSet(sql.toString(), params, e,
              startedAt, preparedAt, queryExecutedAt, System.nanoTime());
          } else {
            FileStorageLogger.errorClosePreparedStatementOnUpdate(sql.toString(), params, e,
              startedAt, preparedAt, System.nanoTime());
          }
        }

        //noinspection ThrowFromFinallyBlock
        throw e;
      }
    }

    if (startedAt != null && FileStorageLogger.isTraceEnabled()) {

      if (queryExecutedAt == null) {

        FileStorageLogger.traceExecuteUpdate(sql.toString(), params, updateCount, startedAt, preparedAt, System.nanoTime());

      } else {

        FileStorageLogger.traceSelect(sql.toString(), params, startedAt, preparedAt, queryExecutedAt, System.nanoTime());

      }

    }

  }

  public void exec(CharSequence sql) throws SQLException {
    if (ps != null) throw new IllegalStateException("Please create new Query");
    long startedAt = System.nanoTime();
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql.toString());
      if (FileStorageLogger.isTraceEnabled()) {
        FileStorageLogger.traceSimpleSql(sql.toString(), startedAt, System.nanoTime());
      }
    } catch (SQLException | RuntimeException e) {
      FileStorageLogger.errorSimpleSql(sql.toString(), e, startedAt, System.nanoTime());
      throw e;
    }
  }
}
