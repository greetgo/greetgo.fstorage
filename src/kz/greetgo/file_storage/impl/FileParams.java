package kz.greetgo.file_storage.impl;

import java.util.Date;

class FileParams {
  String id;
  String sha1sum;
  String name;
  Date lastModifiedBy;
  String mimeType;

  @Override
  public String toString() {
    return "FileParams{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", lastModifiedBy=" + lastModifiedBy +
      ", mimeType='" + mimeType + '\'' +
      '}';
  }
}
