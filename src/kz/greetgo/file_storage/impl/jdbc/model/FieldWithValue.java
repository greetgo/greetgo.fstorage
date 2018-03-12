package kz.greetgo.file_storage.impl.jdbc.model;

public class FieldWithValue extends Field {
  public final Object value;

  public FieldWithValue(String name, Object value) {
    super(name);
    this.value = value;
  }

  @Override
  public String place() {
    return "?";
  }

  @Override
  public String expression() {
    return name + " = ?";
  }
}
