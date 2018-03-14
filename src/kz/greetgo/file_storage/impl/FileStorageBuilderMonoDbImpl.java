package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

import javax.sql.DataSource;

class FileStorageBuilderMonoDbImpl implements FileStorageBuilderMonoDb {
  final FileStorageBuilderImpl parent;
  final DataSource dataSource;
  String dataTable = "file_storage_data";
  String dataTableId = "sha1sum";
  String dataTableData = "data";
  String paramsTable = "file_storage_params";
  String paramsTableId = "id";
  String paramsTableName = "name";
  int paramsTableNameLength = 300;
  String paramsTableDataId = "sha1sum";
  String paramsTableLastModifiedAt = "lastModifiedAt";
  String paramsTableMimeType = "mimeType";
  int paramsTableMimeTypeLength = 50;

  public FileStorageBuilderMonoDbImpl(FileStorageBuilderImpl parent, DataSource dataSource) {
    this.parent = parent;
    this.dataSource = dataSource;
  }

  @Override
  public String getDataTable() {
    return dataTable;
  }

  @Override
  public FileStorageBuilderMonoDb setDataTable(String dataTable) {
    this.dataTable = dataTable;
    return this;
  }

  @Override
  public String getDataTableId() {
    return dataTableId;
  }

  @Override
  public FileStorageBuilderMonoDb setDataTableId(String dataTableId) {
    this.dataTableId = dataTableId;
    return this;
  }

  @Override
  public String getDataTableData() {
    return dataTableData;
  }

  @Override
  public FileStorageBuilderMonoDb setDataTableData(String dataTableData) {
    this.dataTableData = dataTableData;
    return this;
  }

  @Override
  public String getParamsTable() {
    return paramsTable;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTable(String paramsTable) {
    this.paramsTable = paramsTable;
    return this;
  }

  @Override
  public String getParamsTableId() {
    return paramsTableId;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTableId(String paramsTableId) {
    this.paramsTableId = paramsTableId;
    return this;
  }

  @Override
  public String getParamsTableName() {
    return paramsTableName;
  }


  @Override
  public FileStorageBuilderMonoDb setParamsTableName(String paramsTableName) {
    this.paramsTableName = paramsTableName;
    return this;
  }

  @Override
  public int getParamsTableNameLength() {
    return paramsTableNameLength;
  }


  @Override
  public FileStorageBuilderMonoDb setParamsTableNameLength(int paramsTableNameLength) {
    this.paramsTableNameLength = paramsTableNameLength;
    return this;
  }

  @Override
  public String getParamsTableMimeType() {
    return paramsTableMimeType;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTableMimeType(String paramsTableMimeType) {
    this.paramsTableMimeType = paramsTableMimeType;
    return this;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTableMimeTypeLength(int paramsTableMimeTypeLength) {
    this.paramsTableMimeTypeLength = paramsTableMimeTypeLength;
    return this;
  }

  @Override
  public int getParamsTableMimeTypeLength() {
    return paramsTableMimeTypeLength;
  }


  @Override
  public String getParamsTableDataId() {
    return paramsTableDataId;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTableDataId(String paramsTableDataId) {
    this.paramsTableDataId = paramsTableDataId;
    return this;
  }

  @Override
  public String getParamsTableLastModifiedAt() {
    return paramsTableLastModifiedAt;
  }

  @Override
  public FileStorageBuilderMonoDb setParamsTableLastModifiedAt(String paramsTableLastModifiedAt) {
    this.paramsTableLastModifiedAt = paramsTableLastModifiedAt;
    return this;
  }


  @Override
  public FileStorage build() {
    parent.fixed = true;
    return FileStorageCreator.selectDb(
      dataSource,
      () -> new FileStorageMonoDbLogic(parent, new StorageMonoDbDaoPostgres(this)),
      () -> new FileStorageMonoDbLogic(parent, new StorageMonoDbDaoOracle(this))
    );
  }

}
