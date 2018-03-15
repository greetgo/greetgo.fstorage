package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileData;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.TableIsAbsent;

import java.io.InputStream;
import java.util.Date;

import static kz.greetgo.file_storage.impl.StreamUtil.readAll;

public class FileStorageMultiDbLogic implements FileStorage {
  private final FileStorageBuilderImpl parent;
  private final FileStorageBuilderMultiDbImpl builder;
  private final MultiDbOperations operations;

  public FileStorageMultiDbLogic(FileStorageBuilderImpl parent,
                                 FileStorageBuilderMultiDbImpl builder,
                                 MultiDbOperations operations) {
    this.parent = parent;
    this.builder = builder;
    this.operations = operations;
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
      public FileStoringOperation lastModifiedAt(Date lastModifiedAt) {
        params.lastModifiedAt = lastModifiedAt;
        return this;
      }

      @Override
      public FileStoringOperation mimeType(String mimeType) {
        params.mimeType = mimeType;
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
        if (inputStream != null) return readAll(inputStream);
        throw new NoFileData();
      }

      @Override
      public FileStoringOperation presetId(String presetFileId) {
        params.presetFileId = presetFileId;
        return this;
      }

      @Override
      public String store() {
        builder.parent.checkName(params.name);
        builder.parent.checkMimeType(params.mimeType);
        try {
          return createNew(getData(), params);
        } catch (TableIsAbsent e) {
          createTableWithoutThrows(e.tablePosition);
          return createNew(getData(), params);
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

    FileParams params = loadFileParams(fileId);

    if (params == null) return null;

    return new FileDataReader() {
      @Override
      public String name() {
        return params.name;
      }

      final Object sync = new Object();
      byte[] data = null;

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

          return data = loadData(fileId);
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

  ////////////////
  ////////////////
  ////////////////
  ////////////////

  private void createTableWithoutThrows(TablePosition tablePosition) {

  }

  private String createNew(byte[] data, CreateNewParams params) {
    return null;
  }

  private byte[] loadData(String fileId) {
    return new byte[0];
  }

  private FileParams loadFileParams(String fileId) {
    return null;
  }
}
