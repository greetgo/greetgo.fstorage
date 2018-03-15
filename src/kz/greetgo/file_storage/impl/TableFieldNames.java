package kz.greetgo.file_storage.impl;

public class TableFieldNames {
  public String id, name, mimeType, createdAt;

  public String join() {
    return id + ", " + name + ", " + mimeType + ", " + createdAt;
  }
}
