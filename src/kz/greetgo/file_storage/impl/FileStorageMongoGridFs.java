package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileWithId;

public class FileStorageMongoGridFs implements FileStorage {
  private final FileStorageBuilderInMongoGridFsImpl builder;

  public FileStorageMongoGridFs(FileStorageBuilderInMongoGridFsImpl builder) {
    this.builder = builder;
  }

  @Override
  public FileStoringOperation storing() {
    throw new RuntimeException("not impl yet: FileStorageMongoGridFs.storing");
  }

  @Override
  public FileDataReader read(String fileId) throws NoFileWithId {
    throw new RuntimeException("not impl yet: FileStorageMongoGridFs.read");
  }

  @Override
  public FileDataReader readOrNull(String fileId) {
    throw new RuntimeException("not impl yet: FileStorageMongoGridFs.readOrNull");
  }

  @Override
  public void delete(String fileId) throws NoFileWithId {
    throw new RuntimeException("not impl yet: FileStorageMongoGridFs.delete");
  }
}
