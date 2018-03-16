package kz.greetgo.file_storage.impl;

import java.util.Date;

class FileParams {
  String id;
  String sha1sum;
  String name;
  Date createdAt;
  String mimeType;

  @Override
  public String toString() {
    return "FileParams{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", createdAt=" + createdAt +
      ", mimeType='" + mimeType + '\'' +
      '}';
  }
}
