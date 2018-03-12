package kz.greetgo.file_storage.errors;

public class NoFileWithId extends FileStorageError {
  public final String fileId;

  public NoFileWithId(String fileId) {
    super("No file with id = ");
    this.fileId = fileId;
  }
}
