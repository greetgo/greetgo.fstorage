package kz.greetgo.file_storage.impl;

public interface TableSelector {
  TablePosition selectTable(String fileId);
}
