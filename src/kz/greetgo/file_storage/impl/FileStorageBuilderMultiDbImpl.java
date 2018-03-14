package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

import javax.sql.DataSource;
import java.util.List;

public class FileStorageBuilderMultiDbImpl implements FileStorageBuilderMultiDb {
  private final FileStorageBuilderImpl parent;
  private final List<DataSource> dataSourceList;

  public FileStorageBuilderMultiDbImpl(FileStorageBuilderImpl parent, List<DataSource> dataSourceList) {
    this.parent = parent;
    this.dataSourceList = dataSourceList;
  }

  @Override
  public FileStorage build() {
    throw new RuntimeException();
  }

  int tableIndexLength = 5;

  @Override
  public int getTableIndexLength() {
    return tableIndexLength;
  }

  @Override
  public FileStorageBuilderMultiDb setTableIndexLength(int tableIndexLength) {
    parent.checkFix();
    this.tableIndexLength = tableIndexLength;
    return this;
  }

  String tableName = "file_storage";

  @Override
  public String getTableName() {
    return tableName;
  }

  @Override
  public FileStorageBuilderMultiDb setTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  int tableCountPerDb = 12;

  @Override
  public int getTableCountPerDb() {
    return tableCountPerDb;
  }

  @Override
  public FileStorageBuilderMultiDb setTableCountPerDb(int tableCountPerDb) {
    this.tableCountPerDb = tableCountPerDb;
    return this;
  }

  private TableSelector tableSelector = new TableSelector() {
    @Override
    public TablePosition selectTable(String fileId) {
      int hashCode = fileId == null ? 0 : fileId.hashCode();
      if (hashCode < 0) hashCode = -hashCode;
      int tableCount = dataSourceList.size() * tableCountPerDb;
      int tableIndex = hashCode % tableCount;
      int dbIndex = tableIndex % dataSourceList.size();
      return new TablePosition(dbIndex, tableIndex);
    }
  };

  @Override
  public FileStorageBuilderMultiDb setTableSelector(TableSelector tableSelector) {
    if (tableSelector == null) throw new IllegalArgumentException("tableSelector cannot be null");
    this.tableSelector = tableSelector;
    return this;
  }

  @Override
  public TableSelector getTableSelector() {
    return tableSelector;
  }
}