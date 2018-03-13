package kz.greetgo.file_storage.impl.logging;

import kz.greetgo.file_storage.impl.logging.events.ErrorClosePreparedStatementOnUpdate;
import kz.greetgo.file_storage.impl.logging.events.ErrorClosePreparedStatementWithResultSet;
import kz.greetgo.file_storage.impl.logging.events.ErrorCloseResultSet;
import kz.greetgo.file_storage.impl.logging.events.ErrorExecuteQuery;
import kz.greetgo.file_storage.impl.logging.events.ErrorExecuteUpdate;
import kz.greetgo.file_storage.impl.logging.events.ErrorPrepareStatement;
import kz.greetgo.file_storage.impl.logging.events.ErrorSetParameter;
import kz.greetgo.file_storage.impl.logging.events.ErrorSimpleSql;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerErrorEvent;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEvent;
import kz.greetgo.file_storage.impl.logging.events.TraceExecuteUpdate;
import kz.greetgo.file_storage.impl.logging.events.TraceSelect;
import kz.greetgo.file_storage.impl.logging.events.TraceSimpleSql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEventsUtil.doubleToStr;

public class FileStorageLogger {

  private static final AtomicReference<SqlLogger> sqlLogger = new AtomicReference<>(null);

  public static void setNewSqlLogger(SqlLogger newSqlLogger) {
    sqlLogger.set(newSqlLogger);
  }

  public static boolean isTraceEnabled() {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    return x != null && x.isTraceEnabled();
  }

  public static void traceSimpleSql(String sql, long startedAt, long finishedAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.trace(new TraceSimpleSql(sql, startedAt, finishedAt));
  }

  public static void errorSimpleSql(String sql, Exception e, long startedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorSimpleSql(sql, e, startedAt, errorAt));
  }

  public static void errorExecuteQuery(String sql, List<Object> params, Exception e,
                                       long startedAt, long preparedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorExecuteQuery(sql, params, e, startedAt, preparedAt, errorAt));
  }

  public static void errorPrepareStatement(String sql, Exception e, long startedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorPrepareStatement(sql, e, startedAt, errorAt));
  }

  public static void errorSetParameter(String sql, List<Object> params, int settingParameterIndex, Exception e,
                                       long startedAt, long preparedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorSetParameter(sql, params, settingParameterIndex, e,
      startedAt, preparedAt, errorAt));

  }

  public static void errorExecuteUpdate(String sql, List<Object> params, Exception e,
                                        long startedAt, long preparedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorExecuteUpdate(sql, params, e, startedAt, preparedAt, errorAt));
  }

  public static void errorCloseResultSet(String sql, List<Object> params, Exception e,
                                         long startedAt, long preparedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorCloseResultSet(sql, params, e, startedAt, preparedAt, errorAt));
  }

  public static void errorClosePreparedStatementWithResultSet(
    String sql, List<Object> params, Exception e, long startedAt, long preparedAt, long queryExecutedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorClosePreparedStatementWithResultSet(sql, params, e,
      startedAt, preparedAt, queryExecutedAt, errorAt));
  }

  public static void errorClosePreparedStatementOnUpdate(
    String sql, List<Object> params, Exception e, long startedAt, long preparedAt, long errorAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.error(new ErrorClosePreparedStatementOnUpdate(sql, params, e, startedAt, preparedAt, errorAt));
  }

  public static void traceExecuteUpdate(String sql, List<Object> params, int updateCount,
                                        long startedAt, long preparedAt, long finishedAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.trace(new TraceExecuteUpdate(sql, params, updateCount, startedAt, preparedAt, finishedAt));
  }

  public static void traceSelect(String sql, List<Object> params,
                                 long startedAt, long preparedAt, long queryExecutedAt, long finishedAt) {
    SqlLogger x = FileStorageLogger.sqlLogger.get();
    if (x != null) x.trace(new TraceSelect(sql, params, startedAt, preparedAt, queryExecutedAt, finishedAt));
  }

  public static void switchToLog4j(String loggerName) throws Exception {
    Class<?> loggerClass = Class.forName("org.apache.log4j.Logger");
    final Object logger = loggerClass.getMethod("getLogger", String.class).invoke(null, loggerName);
    Method isTraceEnabledMethod = loggerClass.getMethod("isTraceEnabled");
    Method traceMethod = loggerClass.getMethod("trace", Object.class);
    Method errorMethod = loggerClass.getMethod("error", Object.class, Throwable.class);
    sqlLogger.set(new SqlLogger() {
      @Override
      public boolean isTraceEnabled() {
        try {
          return (boolean) isTraceEnabledMethod.invoke(logger);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }

      private String message(FileStorageLoggerEvent event) {
        List<String> ret = new ArrayList<>();

        ret.add(getClass().getSimpleName() + ", delay = " + doubleToStr(event.delayInSeconds()) + "sec");
        List<Object> params = event.params();

        int i = 1;
        if (params.size() == 0) {
          ret.add("No params");
        } else for (Object param : params) {
          ret.add("  param " + i++ + " = " + (param == null ? "< NULL >" : param));
        }
        event.appendMoreInfo(ret);
        ret.add("SQL: " + event.sql());

        return ret.stream().collect(Collectors.joining("\n"));
      }

      @Override
      public void trace(FileStorageLoggerEvent event) {
        try {
          traceMethod.invoke(logger, message(event));
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void error(FileStorageLoggerErrorEvent event) {
        try {
          errorMethod.invoke(logger, message(event), event.error());
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    });

  }
}
