package kz.greetgo.file_storage.impl;

import com.mongodb.MongoClient;
import kz.greetgo.file_storage.FileStorage;

public class FileStorageBuilderInMongoGridFsImpl implements FileStorageBuilderInMongoGridFs {
  final FileStorageBuilderImpl parent;
  final MongoClient mongoClient;
  String bucketName;

  public FileStorageBuilderInMongoGridFsImpl(FileStorageBuilderImpl parent, MongoClient mongoClient) {
    this.parent = parent;
    this.mongoClient = mongoClient;
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
