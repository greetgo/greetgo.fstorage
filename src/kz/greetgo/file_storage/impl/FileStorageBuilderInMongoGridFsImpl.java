package kz.greetgo.file_storage.impl;

import com.mongodb.client.MongoDatabase;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.UnknownMimeType;

import java.util.function.Function;

public class FileStorageBuilderInMongoGridFsImpl implements FileStorageBuilderInMongoGridFs {

  final FileStorageBuilderImpl parent;
  final MongoDatabase database;
  String bucketName;
  Integer chinkSizeBytes;
  boolean useObjectId = true;

  public FileStorageBuilderInMongoGridFsImpl(FileStorageBuilderImpl parent, MongoDatabase database) {
    this.parent = parent;
    this.database = database;
  }

  @Override
  public FileStorageBuilderInMongoGridFs useObjectId(boolean useObjectId) {
    this.useObjectId = useObjectId;
    return this;
  }

  @Override
  public FileStorageBuilderInMongoGridFs bucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  @Override
  public FileStorageBuilderInMongoGridFs chinkSizeBytes(Integer chinkSizeBytes) {
    this.chinkSizeBytes = chinkSizeBytes;
    return this;
  }

  @Override
  public FileStorage build() {
    parent.fixed = true;
    return new FileStorageMongoGridFs(this);
  }

}
