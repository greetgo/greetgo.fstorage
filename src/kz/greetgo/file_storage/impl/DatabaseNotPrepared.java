package kz.greetgo.file_storage.impl;

public class DatabaseNotPrepared extends RuntimeException {
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
