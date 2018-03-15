package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileData;
import kz.greetgo.file_storage.errors.NoFileWithId;

import java.io.InputStream;
import java.util.Date;

public class FileStorageMonoDbLogic implements FileStorage {

  private final FileStorageBuilderImpl builder;
  private final StorageMonoDbDao storageMonoDbDao;

  FileStorageMonoDbLogic(FileStorageBuilderImpl builder, StorageMonoDbDao storageMonoDbDao) {
    this.builder = builder;
    this.storageMonoDbDao = storageMonoDbDao;
  }

  @Override
  public FileStoringOperation storing() {
    return new FileStoringOperation() {
      final CreateNewParams params = new CreateNewParams();

      @Override
      public FileStoringOperation name(String name) {
        params.name = name;
        return this;
      }

      @Override
      public FileStoringOperation mimeType(String mimeType) {
        params.mimeType = mimeType;
        return this;
      }

      @Override
      public FileStoringOperation lastModifiedAt(Date lastModifiedAt) {
        params.lastModifiedAt = lastModifiedAt;
        return this;
      }

      byte[] data = null;
      InputStream inputStream = null;

      @Override
      public FileStoringOperation data(byte[] data) {
        checkSetData();
        this.data = data == null ? new byte[0] : data;
        return this;
      }

      private void checkSetData() {
        if (data != null || inputStream != null) throw new IllegalStateException("data already defined");
      }

      @Override
      public FileStoringOperation data(InputStream inputStream) {
        if (inputStream == null) throw new IllegalArgumentException("inputStream == null");
        checkSetData();
        this.inputStream = inputStream;
        return this;
      }

      private byte[] getData() {
        if (data != null) return data;
        if (inputStream != null) return StreamUtil.readAll(inputStream);
        throw new NoFileData();
      }


      @Override
      public FileStoringOperation presetId(String presetFileId) {
        params.presetFileId = presetFileId;
        return this;
      }

      @Override
      public String store() {
        builder.checkName(params.name);
        builder.checkMimeType(params.mimeType);
        try {
          return storageMonoDbDao.createNew(getData(), params);
        } catch (DatabaseNotPrepared databaseNotPrepared) {
          storageMonoDbDao.prepareDatabase(databaseNotPrepared);
          return storageMonoDbDao.createNew(getData(), params);
        }
      }
    };
  }


  @Override
  public FileDataReader read(String fileId) throws NoFileWithId {

    FileDataReader reader = readOrNull(fileId);

    if (reader == null) throw new NoFileWithId(fileId);

    return reader;
  }

  @Override
  public FileDataReader readOrNull(String fileId) {
    final FileParams params = storageMonoDbDao.readParams(fileId);

    if (params == null) return null;

    return new FileDataReader() {
      @Override
      public String name() {
        return params.name;
      }

      byte[] data = null;

      final Object sync = new Object();

      @Override
      public byte[] dataAsArray() {
        {
          byte[] data = this.data;
          if (data != null) return data;
        }

        synchronized (sync) {
          {
            byte[] data = this.data;
            if (data != null) return data;
          }

          return data = storageMonoDbDao.getDataAsArray(params.sha1sum);
        }
      }

      @Override
      public Date lastModifiedAt() {
        return params.lastModifiedBy;
      }

      @Override
      public String mimeType() {
        return params.mimeType;
      }

      @Override
      public String id() {
        return params.id;
      }
    };
  }
}
