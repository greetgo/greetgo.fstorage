package kz.greetgo.file_storage.errors;

public class FileIdAlreadyExists extends FileStorageError {
  public final String fileId;

  public FileIdAlreadyExists(String fileId) {
    super("fileId = " + fileId);
    this.fileId = fileId;
  }
}
