package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

public interface FileStorageBuilderInMongoGridFs {

  FileStorageBuilderInMongoGridFs bucketName(String bucketName);

  FileStorage build();

}
