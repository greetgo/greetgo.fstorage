package kz.greetgo.file_storage.impl.jdbc.insert;

import java.util.ArrayList;
import java.util.List;

public class Insert {
  public final String tableName;

  public Insert(String tableName) {
    this.tableName = tableName;
  }

  public final List<InsField> fieldList = new ArrayList<>();

  public InsField add(String fieldName, Object value) {
    InsField ret = new InsField();
    ret.name = fieldName;
    ret.value = value;
    fieldList.add(ret);
    return ret;
  }
}
