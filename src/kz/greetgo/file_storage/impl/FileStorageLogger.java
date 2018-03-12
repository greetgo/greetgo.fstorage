package kz.greetgo.file_storage.impl;

import java.util.concurrent.atomic.AtomicReference;

public class FileStorageLogger {

  interface SqlLogger {
    boolean isTraceEnabled();

    void traceSQL(String sql);

    void traceSqlParam(int paramIndex, Object paramValue);
  }

  private static final AtomicReference<SqlLogger> sqlLogger = new AtomicReference<>(null);

  public static void setNewSqlLogger(SqlLogger newSqlLogger) {
    sqlLogger.set(newSqlLogger);
  }

  public static boolean isTraceEnabled() {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    return x != null && x.isTraceEnabled();
  }

  public static void traceSQL(String sql) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.traceSQL(sql);
  }

  public static void traceSqlParam(int paramIndex, Object paramValue) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.traceSqlParam(paramIndex, paramValue);
  }

  public static String view(String sql) {
    if (isTraceEnabled()) {
      traceSQL(sql);
    }
    return sql;
  }
}
