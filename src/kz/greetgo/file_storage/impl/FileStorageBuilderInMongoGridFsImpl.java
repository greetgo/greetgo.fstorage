package kz.greetgo.file_storage.impl;

import com.mongodb.client.MongoDatabase;
import kz.greetgo.file_storage.FileStorage;

public class FileStorageBuilderInMongoGridFsImpl implements FileStorageBuilderInMongoGridFs {

  final FileStorageBuilderImpl parent;
  final MongoDatabase database;
  String bucketName;

  public FileStorageBuilderInMongoGridFsImpl(FileStorageBuilderImpl parent, MongoDatabase database) {
    this.parent = parent;
    this.database = database;
  }

  @Override
  public FileStorageBuilderInMongoGridFs bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  @Override
  public FileStorage build() {
    parent.fixed = true;
    return new FileStorageMongoGridFs(this);
  }
}
