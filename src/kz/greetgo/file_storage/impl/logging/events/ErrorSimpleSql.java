package kz.greetgo.file_storage.impl.logging.events;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorSimpleSql implements FileStorageLoggerErrorEvent {
  private final java.util.Date happenedAt = new java.util.Date();
  public final String sql;
  public final Exception e;
  public final long startedAt;
  public final long errorAt;

  public ErrorSimpleSql(String sql, Exception e, long startedAt, long errorAt) {
    this.sql = sql;
    this.e = e;
    this.startedAt = startedAt;
    this.errorAt = errorAt;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public List<Object> params() {
    return Collections.emptyList();
  }

  @Override
  public long delayInNanos() {
    return errorAt - startedAt;
  }

  @Override
  public Exception error() {
    return e;
  }

  @Override
  public java.util.Date happenedAt() {
    return happenedAt;
  }

  @Override
  public String toString() {
    return info().stream().collect(Collectors.joining("\n"));
  }
}
