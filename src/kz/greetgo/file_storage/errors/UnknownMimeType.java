package kz.greetgo.file_storage.errors;

public class UnknownMimeType extends FileStorageError {
  public final String mimeType;

  public UnknownMimeType(String mimeType) {
    super("mimeType = " + mimeType);
    this.mimeType = mimeType;
  }

  public UnknownMimeType(String mimeType, Exception e) {
    super(e);
    this.mimeType = mimeType;
  }

  public UnknownMimeType(String mimeType, String errorMessage) {
    super(errorMessage);
    this.mimeType = mimeType;
  }
}
