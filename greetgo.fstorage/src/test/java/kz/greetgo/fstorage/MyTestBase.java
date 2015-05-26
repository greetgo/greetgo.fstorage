package kz.greetgo.fstorage;

import kz.greetgo.test.db_providers.TestBase;
import kz.greetgo.util.db.DbType;

import org.testng.annotations.DataProvider;

public abstract class MyTestBase extends TestBase {
  protected static final String DATA_PROVIDER = "dataProvider";
  
  @DataProvider
  public Object[][] dataProvider() throws Exception {
    return super.dataSourceProvider();
  }
  
  @Override
  protected DbType[] usingDbTypes() {
    //    return new DbType[] { DbType.Oracle };
    return DbType.values();
  }
  
  @Override
  protected String dbSchema() {
    return System.getProperty("user.name") + "_ggfstor";
    
  }
  
}
