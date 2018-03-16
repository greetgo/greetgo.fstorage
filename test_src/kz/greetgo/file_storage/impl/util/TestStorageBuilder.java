package kz.greetgo.file_storage.impl.util;

import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.impl.FileStorageBuilder;
import kz.greetgo.file_storage.impl.FileStorageBuilderConfigurator;

import java.util.function.Function;

public abstract class TestStorageBuilder implements FileStorageBuilderConfigurator {
  private boolean mandatoryName = false;
  private boolean mandatoryMimeType = false;

  private Function<String, Boolean> mimeTypeValidator = null;

  public TestStorageBuilder setMimeTypeValidator(Function<String, Boolean> mimeTypeValidator) {
    this.mimeTypeValidator = mimeTypeValidator;
    return this;
  }

  private Function<String, String> mimeTypeExtractor = null;

  public TestStorageBuilder setMimeTypeExtractor(Function<String, String> mimeTypeExtractor) {
    this.mimeTypeExtractor = mimeTypeExtractor;
    return this;
  }

  public Function<String, String> getMimeTypeExtractor() {
    return mimeTypeExtractor;
  }

  public Function<String, Boolean> getMimeTypeValidator() {
    return mimeTypeValidator;
  }

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

  @SuppressWarnings("unused")
  public TestStorageBuilder setTable(String table) {
    this.table = table;
    return this;
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  public TestStorageBuilder setDbCount(int dbCount) {
    this.dbCount = dbCount;
    return this;
  }

  public abstract FileStorageBuilder getBuilder();

  public abstract FileStorage build();

  public abstract String implInfo();

  @Override
  public String toString() {
    return implInfo();
  }

  @Override
  public void configure(FileStorageBuilder builder) {
    builder.mandatoryName(isMandatoryName());
    builder.mandatoryMimeType(isMandatoryMimeType());
    if (mimeTypeValidator != null) builder.mimeTypeValidator(mimeTypeValidator);
    if (mimeTypeExtractor != null) builder.mimeTypeExtractor(mimeTypeExtractor);
  }
}
