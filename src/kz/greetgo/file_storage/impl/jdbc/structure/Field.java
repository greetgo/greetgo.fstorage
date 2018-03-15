package kz.greetgo.file_storage.impl.jdbc.structure;

public class Field {
  public boolean primaryKey = false;
  public String name;
  public FieldType type;
  public int valueLen;
  public boolean notNull = false;
  public boolean defaultCurrentTimestamp = false;
}
