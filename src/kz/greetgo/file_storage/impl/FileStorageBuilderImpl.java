package kz.greetgo.file_storage.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import kz.greetgo.file_storage.errors.MultipleBuilderUsage;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.StorageTypeAlreadySelected;
import kz.greetgo.file_storage.errors.UnknownMimeType;
import org.bson.Document;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

class FileStorageBuilderImpl implements FileStorageBuilder {

  boolean mandatoryName = false;
  boolean mandatoryMimeType = false;

  boolean fixed = false;

  void checkFix() {
    if (fixed) {
      throw new MultipleBuilderUsage();
    }
  }

  @Override
  public FileStorageBuilder mandatoryName(boolean mandatoryName) {
    checkFix();
    this.mandatoryName = mandatoryName;
    return this;
  }

  @Override
  public FileStorageBuilder mandatoryMimeType(boolean mandatoryMimeType) {
    checkFix();
    this.mandatoryMimeType = mandatoryMimeType;
    return this;
  }

  int fileIdLength = 13 + 50;

  private static boolean nullOrEmpty(String str) {
    return str == null || str.length() == 0;
  }

  public void checkName(String name) {
    if (mandatoryName && nullOrEmpty(name)) {
      throw new NoFileName();
    }
  }

  private Supplier<String> idGenerator = null;

  Supplier<String> idGenerator(IdGeneratorType generatorType) {
    Supplier<String> ret = this.idGenerator;
    return ret != null ? ret : generatorType.generator;
  }

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private boolean setIdGeneratorWasCalled = false;

  @Override
  public FileStorageBuilder setIdGenerator(int idLength, Supplier<String> idGenerator) {
    if (idLength < 7) {
      throw new IllegalArgumentException("Must be idLength >= 7: idLength = " + idLength);
    }

    if (idGenerator == null) {
      throw new NullPointerException("idGenerator == null");
    }

    setIdGeneratorWasCalled = true;
    fileIdLength = idLength;
    this.idGenerator = idGenerator;
    return this;
  }

  Function<String, Boolean> mimeTypeValidator = null;

  @Override
  public FileStorageBuilder mimeTypeValidator(Function<String, Boolean> validator) {
    mimeTypeValidator = validator;
    return this;
  }

  Function<String, String> mimeTypeExtractor = null;

  @Override
  public FileStorageBuilder mimeTypeExtractor(Function<String, String> mimeTypeExtractor) {
    this.mimeTypeExtractor = mimeTypeExtractor;
    return this;
  }

  @Override
  public FileStorageBuilder configureFrom(FileStorageBuilderConfigurator configurator) {
    configurator.configure(this);
    return this;
  }

  void checkMimeType(String mimeType) {
    if (mandatoryMimeType && nullOrEmpty(mimeType)) {
      throw new NoFileMimeType();
    }
    if (mimeTypeValidator == null) {
      return;
    }

    try {
      if (mimeTypeValidator.apply(mimeType)) {
        return;
      }
    } catch (RuntimeException e) {
      if (e instanceof UnknownMimeType) {
        throw e;
      }
      throw new UnknownMimeType(mimeType, e);
    }

    throw new UnknownMimeType(mimeType);
  }

  boolean storageTypeSelected = false;

  @Override
  public FileStorageBuilderMonoDb inDb(DataSource dataSource) {
    checkStorageTypeSelected();
    storageTypeSelected = true;
    return new FileStorageBuilderMonoDbImpl(this, dataSource);
  }

  private void checkStorageTypeSelected() {
    if (storageTypeSelected) {
      throw new StorageTypeAlreadySelected();
    }
  }

  @Override
  public FileStorageBuilderMultiDb inMultiDb(List<DataSource> dataSourceList) {
    checkStorageTypeSelected();
    storageTypeSelected = true;
    return new FileStorageBuilderMultiDbImpl(this, dataSourceList);
  }

  @Override
  public FileStorageBuilderInMongodb inMongodb(MongoCollection<Document> collection) {
    checkStorageTypeSelected();
    storageTypeSelected = true;
    return new FileStorageBuilderInMongodbImpl(this, collection);
  }

  @Override
  public FileStorageBuilderInMongoGridFs inMongoGridFs(MongoDatabase database) {
    Objects.requireNonNull(database, "Not specified database (== null)");
    checkStorageTypeSelected();
    storageTypeSelected = true;
    return new FileStorageBuilderInMongoGridFsImpl(this, database);
  }


  String validateMimeType(String mimeType) {
    if (mandatoryMimeType && mimeType == null) {
      throw new NoFileMimeType();
    }

    if (mimeType != null) {
      Function<String, Boolean> mimeTypeValidator = this.mimeTypeValidator;
      if (mimeTypeValidator != null) {
        try {
          if (!mimeTypeValidator.apply(mimeType)) {
            throw new UnknownMimeType(mimeType);
          }
        } catch (Exception e) {
          throw new UnknownMimeType(mimeType, e);
        }
      }
    }

    return mimeType;
  }
}
