package kz.greetgo.file_storage.impl.util;

public class UserExistsError extends RuntimeException {
  public UserExistsError(String username) {
    super("username = " + username);
  }
}
