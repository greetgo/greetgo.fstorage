package kz.greetgo.file_storage.impl.logging.events;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TraceSimpleSql implements FileStorageLoggerEvent {
  private final java.util.Date happenedAt = new java.util.Date();
  public final String sql;
  public final long startedAt;
  public final long finishedAt;

  public TraceSimpleSql(String sql, long startedAt, long finishedAt) {
    this.sql = sql;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
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
