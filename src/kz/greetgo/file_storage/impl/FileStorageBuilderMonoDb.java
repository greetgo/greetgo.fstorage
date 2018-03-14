package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

/**
 * Строитель хранилища файлов в БД
 */
public interface FileStorageBuilderMonoDb {

  String getDataTable();

  FileStorageBuilderMonoDb setDataTable(String dataTable);

  String getDataTableId();

  FileStorageBuilderMonoDb setDataTableId(String dataTableId);

  String getDataTableData();

  FileStorageBuilderMonoDb setDataTableData(String dataTableData);

  String getParamsTable();

  FileStorageBuilderMonoDb setParamsTable(String paramsTable);

  String getParamsTableId();

  String getParamsTableName();

  FileStorageBuilderMonoDb setParamsTableId(String paramsTableId);

  int getParamsTableNameLength();

  FileStorageBuilderMonoDb setParamsTableNameLength(int paramsTableNameLength);

  FileStorageBuilderMonoDb setParamsTableName(String paramsTableName);

  String getParamsTableDataId();

  FileStorageBuilderMonoDb setParamsTableDataId(String paramsTableDataId);

  String getParamsTableLastModifiedAt();

  FileStorageBuilderMonoDb setParamsTableLastModifiedAt(String paramsTableLastModifiedAt);


  String getParamsTableMimeType();

  FileStorageBuilderMonoDb setParamsTableMimeType(String paramsTableMimeType);

  FileStorageBuilderMonoDb setParamsTableMimeTypeLength(int paramsTableMimeTypeLength);

  int getParamsTableMimeTypeLength();

  FileStorage build();
}
