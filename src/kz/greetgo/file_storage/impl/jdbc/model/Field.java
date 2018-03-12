package kz.greetgo.file_storage.impl.jdbc.model;

public abstract class Field {
  public final String name;

  Field(String name) {
    this.name = name;
  }

  public abstract String place();

  public abstract String expression();
}
