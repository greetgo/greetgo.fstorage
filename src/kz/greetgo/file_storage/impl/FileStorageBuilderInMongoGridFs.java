package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

public interface FileStorageBuilderInMongoGridFs {

  FileStorageBuilderInMongoGridFs bucketName(String bucketName);

  FileStorageBuilderInMongoGridFs chinkSizeBytes(Integer chinkSizeBytes);

  FileStorageBuilderInMongoGridFs useObjectId(boolean useObjectId);

  FileStorage build();

}
