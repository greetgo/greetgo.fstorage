package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProvidersForTests {
  protected DbType[] dbTypes() {
    return new DbType[]{DbType.Postgres, DbType.Oracle};
  }

  @DataProvider
  public Object[][] dbTypeDataProvider() {
    DbType[] dbTypes = dbTypes();
    Object[][] ret = new Object[dbTypes.length][];
    for (int i = 0; i < dbTypes.length; i++) {
      ret[i] = new Object[]{dbTypes[i]};
    }
    return ret;
  }
}