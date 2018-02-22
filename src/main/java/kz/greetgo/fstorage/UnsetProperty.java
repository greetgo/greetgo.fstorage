package kz.greetgo.fstorage;

public class UnsetProperty extends RuntimeException {
  
  public final String propertyName;
  
  public UnsetProperty(String propertyName) {
    super("Unset property " + propertyName);
    this.propertyName = propertyName;
  }
}
