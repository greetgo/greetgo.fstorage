package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
  public FileStorage build() {
    parent.fixed = true;

    try (Connection connection = dataSource.getConnection()) {
      String db = connection.getMetaData().getDatabaseProductName().toLowerCase();
      if ("oracle".equals(db)) return buildForOracle();
      if ("postgresql".equals(db)) return buildForPostgres();
      throw new RuntimeException("Cannot detect DB type: db = " + db);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

  private FileStorage buildForOracle() {
    return new FileStorageBridge(parent, new StorageDaoOracle(this));
  }

  private FileStorage buildForPostgres() {
    return new FileStorageBridge(parent, new StorageDaoPostgres(this));
  }
}