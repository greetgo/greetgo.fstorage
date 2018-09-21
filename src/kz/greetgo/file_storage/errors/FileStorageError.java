package kz.greetgo.file_storage.errors;

public class FileStorageError extends RuntimeException {
  public FileStorageError(String message) {
    super(message);
  }

  public FileStorageError(Exception e) {
    super(e);
  }
}
