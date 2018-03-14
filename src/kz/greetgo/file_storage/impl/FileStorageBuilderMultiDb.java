package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

public interface FileStorageBuilderMultiDb {
  FileStorage build();

  int getTableIndexLength();

  FileStorageBuilderMultiDb setTableIndexLength(int tableIndexLength);

  String getTableName();

  FileStorageBuilderMultiDb setTableName(String tableName);

  int getTableCountPerDb();

  FileStorageBuilderMultiDb setTableCountPerDb(int tableCountPerDb);

  FileStorageBuilderMultiDb setTableSelector(TableSelector tableSelector);

  TableSelector getTableSelector();
}
