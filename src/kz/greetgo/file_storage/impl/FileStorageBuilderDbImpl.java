package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.util.db.DbType;
import kz.greetgo.util.db.DbTypeDetector;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

class FileStorageBuilderDbImpl implements FileStorageBuilderDb {
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

  public FileStorageBuilderDbImpl(FileStorageBuilderImpl parent, DataSource dataSource) {
    this.parent = parent;
    this.dataSource = dataSource;
  }

  @Override
  public String getDataTable() {
    return dataTable;
  }

  @Override
  public FileStorageBuilderDb setDataTable(String dataTable) {
    this.dataTable = dataTable;
    return this;
  }

  @Override
  public String getDataTableId() {
    return dataTableId;
  }

  @Override
  public FileStorageBuilderDb setDataTableId(String dataTableId) {
    this.dataTableId = dataTableId;
    return this;
  }

  @Override
  public String getDataTableData() {
    return dataTableData;
  }

  @Override
  public FileStorageBuilderDb setDataTableData(String dataTableData) {
    this.dataTableData = dataTableData;
    return this;
  }

  @Override
  public String getParamsTable() {
    return paramsTable;
  }

  @Override
  public FileStorageBuilderDb setParamsTable(String paramsTable) {
    this.paramsTable = paramsTable;
    return this;
  }

  @Override
  public String getParamsTableId() {
    return paramsTableId;
  }

  @Override
  public FileStorageBuilderDb setParamsTableId(String paramsTableId) {
    this.paramsTableId = paramsTableId;
    return this;
  }

  @Override
  public String getParamsTableName() {
    return paramsTableName;
  }


  @Override
  public FileStorageBuilderDb setParamsTableName(String paramsTableName) {
    this.paramsTableName = paramsTableName;
    return this;
  }

  @Override
  public int getParamsTableNameLength() {
    return paramsTableNameLength;
  }


  @Override
  public FileStorageBuilderDb setParamsTableNameLength(int paramsTableNameLength) {
    this.paramsTableNameLength = paramsTableNameLength;
    return this;
  }

  @Override
  public String getParamsTableMimeType() {
    return paramsTableMimeType;
  }

  @Override
  public FileStorageBuilderDb setParamsTableMimeType(String paramsTableMimeType) {
    this.paramsTableMimeType = paramsTableMimeType;
    return this;
  }

  @Override
  public FileStorageBuilderDb setParamsTableMimeTypeLength(int paramsTableMimeTypeLength) {
    this.paramsTableMimeTypeLength = paramsTableMimeTypeLength;
    return this;
  }

  @Override
  public int getParamsTableMimeTypeLength() {
    return paramsTableMimeTypeLength;
  }

  @Override
  public FileStorageBuilderDb mimeTypeValidator(Function<String, Boolean> validator) {
    parent.mimeTypeValidator(validator);
    return this;
  }

  @Override
  public String getParamsTableDataId() {
    return paramsTableDataId;
  }

  @Override
  public FileStorageBuilderDb setParamsTableDataId(String paramsTableDataId) {
    this.paramsTableDataId = paramsTableDataId;
    return this;
  }

  @Override
  public String getParamsTableLastModifiedAt() {
    return paramsTableLastModifiedAt;
  }

  @Override
  public FileStorageBuilderDb setParamsTableLastModifiedAt(String paramsTableLastModifiedAt) {
    this.paramsTableLastModifiedAt = paramsTableLastModifiedAt;
    return this;
  }

  @Override
  public FileStorageBuilderDb mandatoryName(boolean mandatoryName) {
    parent.mandatoryName(mandatoryName);
    return this;
  }

  @Override
  public FileStorageBuilderDb mandatoryMimeType(boolean mandatoryMimeType) {
    parent.mandatoryMimeType(mandatoryMimeType);
    return this;
  }

  @Override
  public FileStorageBuilderDb idGenerator(int idLength, Supplier<String> idGenerator) {
    parent.idGenerator(idLength, idGenerator);
    return this;
  }

  @Override
  public FileStorageBuilderDb inDb(DataSource dataSource) {
    throw new IllegalStateException("Storage type, you already has been defined");//Yoda style!
  }

  @Override
  public FileStorage build() {
    parent.fixed = true;
    try {
      DbType dbType = DbTypeDetector.detect(dataSource);
      switch (dbType) {
        case PostgreSQL:
          return new FileStorageBridge(parent, new StorageDaoPostgres(this));
        case Oracle:
          return new FileStorageBridge(parent, new StorageDaoOracle(this));
        default:
          throw new RuntimeException("No storage for DB " + dbType);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }
}
