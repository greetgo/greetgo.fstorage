package kz.greetgo.fstorage;

public class FStorageConfig {
  public final String tableName;
  public final int tableCount;
  
  public boolean hasCreatedAt = false;
  
  public FStorageConfig(String tableName, int tableCount) {
    this.tableName = tableName;
    this.tableCount = tableCount;
  }
}
