package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.NoFileWithId;

public interface MonoDbOperations {
  String createNew(byte[] data, CreateNewParams params) throws DatabaseNotPrepared;

  void prepareDatabase(DatabaseNotPrepared context);

  FileParams readParams(String fileId);

  byte[] getDataAsArray(String sha1sum);

  void delete(String fileId) throws NoFileWithId;
}
