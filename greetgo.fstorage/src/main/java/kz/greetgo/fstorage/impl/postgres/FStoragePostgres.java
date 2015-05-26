package kz.greetgo.fstorage.impl.postgres;

import javax.sql.DataSource;

import kz.greetgo.fstorage.impl.AbstractFStorage;
import kz.greetgo.fstorage.impl.FStorageConfig;

public class FStoragePostgres extends AbstractFStorage {
  
  public FStoragePostgres(DataSource dataSource, FStorageConfig config) {
    super(dataSource, config);
  }
  
  @Override
  protected String nextIdSql(String sequenceName) {
    return "select nextval('" + sequenceName + "')";
  }
  
  @Override
  protected String fieldTypeId() {
    return "int8";
  }
  
  @Override
  protected String fieldTypeFilename() {
    return "varchar(" + fieldFilenameLen + ")";
  }
  
  @Override
  protected String fieldTypeData() {
    return "bytea";
  }
  
  @Override
  protected String fieldTypeCreatedAt() {
    return "timestamp";
  }
  
  @Override
  protected String fieldTypeSize() {
    return "int8";
  }
  
  @Override
  protected String currentTimestampFunc() {
    return "current_timestamp";
  }
  
}
