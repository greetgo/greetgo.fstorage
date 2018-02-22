package kz.greetgo.fstorage;

public class FStorageConfig {
  public final String tableName;
  public final int tableCount;

  public final boolean hasCreatedAt;

  public FStorageConfig(String tableName, int tableCount, boolean hasCreatedAt) {
    this.tableName = tableName;
    this.tableCount = tableCount;
    this.hasCreatedAt = hasCreatedAt;
  }
}
