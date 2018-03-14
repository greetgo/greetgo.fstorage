package kz.greetgo.file_storage.impl;

public interface StorageMonoDbDao {
  String createNew(byte[] data, CreateNewParams params) throws DatabaseNotPrepared;

  void prepareDatabase(DatabaseNotPrepared context);

  FileParams readParams(String fileId);

  byte[] getDataAsArray(String sha1sum);
}
