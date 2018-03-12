package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;

class PostgresDataSourceFileStorage implements FileStorage {
  private final FileStorageBuilderDbImpl builder;

  public PostgresDataSourceFileStorage(FileStorageBuilderDbImpl builder) {
    this.builder = builder;
  }

  @Override
  public FileStoringOperation storing() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileDataReader read(String fileId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileDataReader readOrNull(String fileId) {
    throw new UnsupportedOperationException();
  }
}
