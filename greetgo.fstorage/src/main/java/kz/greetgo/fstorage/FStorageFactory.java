package kz.greetgo.fstorage;

import static kz.greetgo.util.db.DbTypeDetector.detect;

import java.sql.SQLException;

import javax.sql.DataSource;

import kz.greetgo.fstorage.impl.AbstractFStorage;
import kz.greetgo.fstorage.impl.FStorageConfig;
import kz.greetgo.fstorage.impl.oracle.FStorageOracle;
import kz.greetgo.fstorage.impl.postgres.FStoragePostgres;
import kz.greetgo.util.db.DbType;

public class FStorageFactory {
  private int fieldFilenameLen = 300;
  private DataSource dataSource = null;
  private FStorageConfig config;
  
  public void setConfig(FStorageConfig config) {
    this.config = config;
  }
  
  public int getFieldFilenameLen() {
    return fieldFilenameLen;
  }
  
  public void setFieldFilenameLen(int fieldFilenameLen) {
    this.fieldFilenameLen = fieldFilenameLen;
  }
  
  public DataSource getDataSource() {
    return dataSource;
  }
  
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  public String getTableName() {
    return config.tableName;
  }
  
  public int getTableCount() {
    return config.tableCount;
  }
  
  public FStorage create() throws SQLException {
    check();
    DbType dbType = detect(dataSource);
    
    switch (dbType) {
    case PostgreSQL:
      return prepare(new FStoragePostgres(dataSource, config));
      
    case Oracle:
      return prepare(new FStorageOracle(dataSource, config));
      
    default:
      throw new IllegalArgumentException("Cannot create FStorage for DbType = " + dbType);
    }
    
  }
  
  private AbstractFStorage prepare(AbstractFStorage ret) {
    ret.fieldFilenameLen = fieldFilenameLen;
    return ret;
  }
  
  private void check() {
    if (dataSource == null) throw new UnsetProperty("dataSource");
    if (config == null) throw new UnsetProperty("config");
    if (config.tableName == null) throw new UnsetProperty("config.tableName");
    if (config.tableCount <= 0) throw new UnsetProperty("config.tableCount");
  }
  
}
