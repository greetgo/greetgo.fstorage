package kz.greetgo.file_storage.impl.logging.events;

import java.util.List;
import java.util.stream.Collectors;

public class TraceSelect implements FileStorageLoggerEvent {
  private final java.util.Date happenedAt = new java.util.Date();
  public final String sql;
  public final List<Object> params;
  public final long startedAt;
  public final long preparedAt;
  public final long queryExecutedAt;
  public final long finishedAt;

  public TraceSelect(String sql, List<Object> params,
                     long startedAt, long preparedAt, long queryExecutedAt, long finishedAt) {
    this.sql = sql;
    this.params = params;
    this.startedAt = startedAt;
    this.preparedAt = preparedAt;
    this.queryExecutedAt = queryExecutedAt;
    this.finishedAt = finishedAt;
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
    return finishedAt - startedAt;
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
