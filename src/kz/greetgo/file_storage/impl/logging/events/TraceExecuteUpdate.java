package kz.greetgo.file_storage.impl.logging.events;

import java.util.List;
import java.util.stream.Collectors;

public class TraceExecuteUpdate implements FileStorageLoggerEvent {
  private final java.util.Date happenedAt = new java.util.Date();
  public final String sql;
  public final List<Object> params;
  public int updateCount;
  public final long startedAt;
  public final long preparedAt;
  public final long finishedAt;

  public TraceExecuteUpdate(String sql, List<Object> params, int updateCount, long startedAt, long preparedAt, long finishedAt) {
    this.sql = sql;
    this.params = params;
    this.updateCount = updateCount;
    this.startedAt = startedAt;
    this.preparedAt = preparedAt;
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
  public void appendMoreInfo(List<String> infoList) {
    infoList.add("  update count = " + updateCount);
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
