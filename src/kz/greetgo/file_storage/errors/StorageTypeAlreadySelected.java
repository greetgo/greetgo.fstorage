package kz.greetgo.file_storage.errors;

public class StorageTypeAlreadySelected extends RuntimeException {
  public StorageTypeAlreadySelected() {
    super("You already selected storage type. Create new builder and select another");
  }
}
