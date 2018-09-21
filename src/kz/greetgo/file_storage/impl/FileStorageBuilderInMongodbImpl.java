package kz.greetgo.file_storage.impl;

import com.mongodb.client.MongoCollection;
import kz.greetgo.file_storage.FileStorage;
import org.bson.Document;

public class FileStorageBuilderInMongodbImpl implements FileStorageBuilderInMongodb {
  final FileStorageBuilderImpl parent;
  final MongoCollection<Document> collection;

  static class Names {
    String id = "id";
    String content = "content";
    String name = "name";
    String mimeType = "mimeType";
    String createdAt = "createdAt";
  }

  final Names names = new Names();

  FileStorageBuilderInMongodbImpl(FileStorageBuilderImpl parent, MongoCollection<Document> collection) {
    this.parent = parent;
    this.collection = collection;
  }

  @Override
  public FileStorage build() {
    parent.fixed = true;
    return new FileStorageMongodb(this);
  }
}
