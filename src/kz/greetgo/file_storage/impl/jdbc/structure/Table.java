package kz.greetgo.file_storage.impl.jdbc.structure;

import java.util.ArrayList;
import java.util.List;

public class Table {
  public final String name;

  public Table(String name) {
    this.name = name;
  }

  public final List<Field> fieldList = new ArrayList<>();

  public Field addField() {
    Field ret = new Field();
    fieldList.add(ret);
    return ret;
  }
}
