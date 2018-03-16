package kz.greetgo.file_storage.impl.util;

import kz.greetgo.file_storage.FileStorage;

public abstract class TestStorageBuilder {
  private boolean mandatoryName = false;
  private boolean mandatoryMimeType = false;

  public TestStorageBuilder setNameMandatory(boolean mandatoryName) {
    this.mandatoryName = mandatoryName;
    return this;
  }

  public boolean isMandatoryName() {
    return mandatoryName;
  }

  public boolean isMandatoryMimeType() {
    return mandatoryMimeType;
  }

  public TestStorageBuilder setMimeTypeMandatory(boolean mandatoryMimeType) {
    this.mandatoryMimeType = mandatoryMimeType;
    return this;
  }

  private String table = "fs2", schema = "t1";

  public TestStorageBuilder setTable(String table) {
    this.table = table;
    return this;
  }

  public TestStorageBuilder setSchema(String schema) {
    this.schema = schema;
    return this;
  }

  public String getSchema() {
    return schema;
  }

  public String getTable() {
    return table;
  }

  private int dbCount = 4;

  public int getDbCount() {
    return dbCount;
  }

  public TestStorageBuilder setDbCount(int dbCount) {
    this.dbCount = dbCount;
    return this;
  }

  public abstract FileStorage build();

  public abstract String implInfo();

  @Override
  public String toString() {
    return implInfo() + ", " + table + "." + schema;
  }
}
