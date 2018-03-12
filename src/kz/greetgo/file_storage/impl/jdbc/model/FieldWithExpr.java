package kz.greetgo.file_storage.impl.jdbc.model;

public class FieldWithExpr extends Field {
  public final String expression;

  public FieldWithExpr(String name, String expression) {
    super(name);
    this.expression = expression;
  }

  @Override
  public String place() {
    return expression;
  }

  @Override
  public String expression() {
    return name == null ? expression : name + " = " + expression;
  }
}
