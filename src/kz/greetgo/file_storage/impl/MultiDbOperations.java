package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.TableIsAbsent;
import kz.greetgo.file_storage.impl.jdbc.insert.Insert;
import kz.greetgo.file_storage.impl.jdbc.structure.Table;

import javax.sql.DataSource;

public interface MultiDbOperations {
  void createTableQuiet(DataSource dataSource, Table table);

  void insert(DataSource dataSource, Insert insert, TablePosition tablePosition) throws TableIsAbsent;

  byte[] loadData(DataSource dataSource, String tableName, String idFieldName, String idValue, String gettingFieldName);

  FileParams loadFileParams(DataSource dataSource, String tableName, String fileId, TableFieldNames names);

  void delete(DataSource dataSource, String tableName, String fileIdField, String fileId, TablePosition tablePosition);
}
