package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

/**
 * Строитель хранилища файлов в БД
 */
public interface FileStorageBuilderDb {

  String getDataTable();

  FileStorageBuilderDb setDataTable(String dataTable);

  String getDataTableId();

  FileStorageBuilderDb setDataTableId(String dataTableId);

  String getDataTableData();

  FileStorageBuilderDb setDataTableData(String dataTableData);

  String getParamsTable();

  FileStorageBuilderDb setParamsTable(String paramsTable);

  String getParamsTableId();

  String getParamsTableName();

  FileStorageBuilderDb setParamsTableId(String paramsTableId);

  int getParamsTableNameLength();

  FileStorageBuilderDb setParamsTableNameLength(int paramsTableNameLength);

  FileStorageBuilderDb setParamsTableName(String paramsTableName);

  String getParamsTableDataId();

  FileStorageBuilderDb setParamsTableDataId(String paramsTableDataId);

  String getParamsTableLastModifiedAt();

  FileStorageBuilderDb setParamsTableLastModifiedAt(String paramsTableLastModifiedAt);


  String getParamsTableMimeType();

  FileStorageBuilderDb setParamsTableMimeType(String paramsTableMimeType);

  FileStorageBuilderDb setParamsTableMimeTypeLength(int paramsTableMimeTypeLength);

  int getParamsTableMimeTypeLength();

  FileStorage build();
}
