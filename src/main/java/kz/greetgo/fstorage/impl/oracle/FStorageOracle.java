package kz.greetgo.fstorage.impl.oracle;

import javax.sql.DataSource;

import kz.greetgo.fstorage.FStorageConfig;
import kz.greetgo.fstorage.impl.AbstractFStorage;

public class FStorageOracle extends AbstractFStorage {
  
  public FStorageOracle(DataSource dataSource, FStorageConfig config) {
    super(dataSource, config);
  }
  
  @Override
  protected String nextIdSql(String sequenceName) {
    return "select " + sequenceName + ".nextval from dual";
  }
  
  @Override
  protected String fieldTypeId() {
    return "number(19)";
  }
  
  @Override
  protected String fieldTypeFilename() {
    return "varchar2(" + fieldFilenameLen + ")";
  }
  
  @Override
  protected String fieldTypeData() {
    return "blob";
  }
  
  @Override
  protected String fieldTypeCreatedAt() {
    return "timestamp";
  }
  
  @Override
  protected String fieldTypeSize() {
    return "number(19)";
  }
  
  @Override
  protected String currentTimestampFunc() {
    return "systimestamp";
  }
  
}