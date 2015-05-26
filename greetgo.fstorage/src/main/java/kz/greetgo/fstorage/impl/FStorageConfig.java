package kz.greetgo.fstorage.impl;

public class FStorageConfig {
  public final String tableName;
  public final int tableCount;
  
  public boolean hasCreatedAt = false;
  public boolean hasSize = false;
  
  public FStorageConfig(String tableName, int tableCount) {
    this.tableName = tableName;
    this.tableCount = tableCount;
  }
}
