package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.impl.logging.FileStorageLogger;
import kz.greetgo.file_storage.impl.logging.SqlLogger;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerErrorEvent;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEvent;
import kz.greetgo.file_storage.impl.util.MongodbUtil;
import kz.greetgo.file_storage.impl.util.TestStorageBuilder;
import kz.greetgo.file_storage.impl.util.TestUtil;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static kz.greetgo.file_storage.impl.util.MongodbUtil.connectGetCollection;
import static kz.greetgo.file_storage.impl.util.MongodbUtil.createDatabase;
import static kz.greetgo.file_storage.impl.util.MongodbUtil.createMongoClient;

public class DataProvidersForTests {
  @BeforeMethod
  public void setNewSqlLogger_to_FileStorageLogger() {
    FileStorageLogger.setNewSqlLogger(new SqlLogger() {
      @Override
      public boolean isTraceEnabled() {
        return traceSql();
      }

      @Override
      public void trace(FileStorageLoggerEvent event) {
        System.out.println(event);
      }

      @Override
      public void error(FileStorageLoggerErrorEvent event) {
        if (traceSql()) { System.out.println(event); }
      }
    });
  }

  protected boolean traceSql() {
    return true;
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

  private TestStorageBuilder testBuilderForMonoDb(DbType dbType) {
    return new TestStorageBuilder() {
      FileStorageBuilder builder = FileStorageBuilder.newBuilder();

      @Override
      public FileStorageBuilder getBuilder() {
        return builder;
      }

      @Override
      public FileStorage build() {
        return builder
          .configureFrom(this)
          .inDb(TestUtil.createFrom(dbType, getSchema()))
          .setDataTable(getTable() + "_data")
          .setParamsTable(getTable() + "_param")
          .build();
      }

      @Override
      public String implInfo() {
        return "MonoDb, " + dbType;
      }
    };
  }

  private TestStorageBuilder testBuilderForMongodb() {
    return new TestStorageBuilder() {
      final FileStorageBuilder builder = FileStorageBuilder.newBuilder();

      @Override
      public FileStorageBuilder getBuilder() {
        return builder;
      }

      @Override
      public FileStorage build() {

        if (!MongodbUtil.hasMongodb()) {
          throw new SkipException("Cannot access to MongoDB");
        }

        return getBuilder()
          .configureFrom(this)
          .inMongodb(connectGetCollection(getTable()))
          .build()
          ;
      }

      @Override
      public String implInfo() {
        return "MongoDB";
      }
    };
  }

  private TestStorageBuilder testBuilderForMongoGridFs() {
    return new TestStorageBuilder() {
      final FileStorageBuilder builder = FileStorageBuilder.newBuilder();

      @Override
      public FileStorageBuilder getBuilder() {
        return builder;
      }

      @Override
      public FileStorage build() {

        if (!MongodbUtil.hasMongodb()) {
          throw new SkipException("Cannot access to MongoDB");
        }

        return getBuilder()
          .configureFrom(this)
          .inMongoGridFs(createDatabase("test_grid_fs"))
          .bucketName(getTable())
          .build()
          ;
      }

      @Override
      public String implInfo() {
        return "MongoDB GridFS";
      }
    };
  }

  private TestStorageBuilder testBuilderForMultiDb(DbType dbType) {
    return new TestStorageBuilder() {
      FileStorageBuilder builder = FileStorageBuilder.newBuilder();

      @Override
      public FileStorageBuilder getBuilder() {
        return builder;
      }

      @Override
      public FileStorage build() {
        return builder
          .configureFrom(this)
          .inMultiDb(dataSourceList(dbType, getSchema(), getDbCount()))
          .setTableName(getTable())
          .build();
      }

      @Override
      public String implInfo() {
        return "MultiDb " + getDbCount() + ", " + dbType;
      }
    };
  }

  @DataProvider
  public Object[][] testStorageBuilder_DP() {
    return new Object[][]{
      {testBuilderForMonoDb(DbType.Postgres)},
      {testBuilderForMonoDb(DbType.Oracle)},
      {testBuilderForMultiDb(DbType.Postgres)},
      {testBuilderForMultiDb(DbType.Oracle)},
      {testBuilderForMongodb()},
      {testBuilderForMongoGridFs()},
    };
  }
}
