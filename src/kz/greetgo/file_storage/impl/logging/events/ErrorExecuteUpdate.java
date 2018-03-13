package kz.greetgo.file_storage.impl.logging.events;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorExecuteUpdate implements FileStorageLoggerErrorEvent {
  private final java.util.Date happenedAt = new java.util.Date();
  public final String sql;
  public final List<Object> params;
  public final Exception e;
  public final long startedAt;
  public final long preparedAt;
  public final long errorAt;

  public ErrorExecuteUpdate(String sql, List<Object> params, Exception e, long startedAt, long preparedAt, long errorAt) {
    this.sql = sql;
    this.params = params;
    this.e = e;
    this.startedAt = startedAt;
    this.preparedAt = preparedAt;
    this.errorAt = errorAt;
  }

  @Override
  public Exception error() {
    return e;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public List<Object> params() {
    return params;
  }

  @Override
  public long delayInNanos() {
    return errorAt - startedAt;
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
