package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.impl.logging.FileStorageLogger;
import kz.greetgo.file_storage.impl.logging.SqlLogger;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerErrorEvent;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEvent;
import kz.greetgo.file_storage.impl.util.TestUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DataProvidersForTests {
  @BeforeMethod
  public void setNewSqlLogger_to_FileStorageLogger() throws Exception {
    FileStorageLogger.setNewSqlLogger(new SqlLogger() {
      @Override
      public boolean isTraceEnabled() {
        return true;
      }

      @Override
      public void trace(FileStorageLoggerEvent event) {
        System.out.println(event);
      }

      @Override
      public void error(FileStorageLoggerErrorEvent event) {
        System.out.println(event);
      }


    });
  }

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

  @SuppressWarnings("SameParameterValue")
  protected List<DataSource> dataSourceList(DbType dbType, String schemaPrefix, int count) {
    List<DataSource> ret = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      ret.add(TestUtil.createFrom(dbType, schemaPrefix + "_" + i));
    }
    return ret;
  }

  @SuppressWarnings("SameParameterValue")
  private FileStorageCreator dbFileStorage(DbType dbType, String schema, String tableNamePrefix, boolean nameMandatory) {
    return () -> FileStorageBuilder
      .newBuilder()
      .mandatoryName(nameMandatory)
      .inDb(TestUtil.createFrom(dbType, schema))
      .setDataTable(tableNamePrefix + "_data")
      .setParamsTable(tableNamePrefix + "_param")
      .build();
  }

  @SuppressWarnings("SameParameterValue")
  private FileStorageCreator multiDbFileStorage(DbType dbType, String schema, int dbCount,
                                                String tableName, boolean nameMandatory) {
    return () -> FileStorageBuilder
      .newBuilder()
      .mandatoryName(nameMandatory)
      .inMultiDb(dataSourceList(dbType, schema, dbCount))
      .setTableName(tableName)
      .build();
  }

  @DataProvider
  public Object[][] fileStorageWithNameMandatory() {
    return new Object[][]{
      {dbFileStorage(DbType.Postgres, "fs2", "t1", true)},
      {dbFileStorage(DbType.Oracle, "fs2", "t1", true)},
      {multiDbFileStorage(DbType.Postgres, "fs2", 4, "t1", true)},
      {multiDbFileStorage(DbType.Oracle, "fs2", 4, "t1", true)},
    };
  }

  @DataProvider
  public Object[][] fileStorageWithNameNotMandatory() {
    return new Object[][]{
      {dbFileStorage(DbType.Postgres, "fs2", "t1", false)},
      {dbFileStorage(DbType.Oracle, "fs2", "t1", false)},
      {multiDbFileStorage(DbType.Postgres, "fs2", 4, "t1", false)},
      {multiDbFileStorage(DbType.Oracle, "fs2", 4, "t1", false)},
    };
  }
}
