package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.impl.util.RND;
import kz.greetgo.file_storage.impl.util.TestUtil;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderMultiDbTest extends DataProvidersForTests {
  public static final String SCHEMA_PREFIX = "multi_fs";

  private List<DataSource> dataSourceList(DbType dbType, int count) {
    List<DataSource> ret = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      ret.add(TestUtil.createFrom(dbType, SCHEMA_PREFIX + "_" + i));
    }
    return ret;
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableIndexLength(DbType dbType) throws Exception {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, 3));

    assertThat(builder.getTableIndexLength()).isEqualTo(5);

    builder = builder.setTableIndexLength(7);

    assertThat(builder.getTableIndexLength()).isEqualTo(7);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableName(DbType dbType) throws Exception {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, 3));

    assertThat(builder.getTableName()).isEqualTo("file_storage");

    builder = builder.setTableName("left_name");

    assertThat(builder.getTableName()).isEqualTo("left_name");
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableCountPerDb(DbType dbType) throws Exception {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, 3));

    assertThat(builder.getTableCountPerDb()).isEqualTo(12);

    builder = builder.setTableCountPerDb(42);

    assertThat(builder.getTableCountPerDb()).isEqualTo(42);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableDetector(DbType dbType) throws Exception {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, 3));

    assertThat(builder.getTableSelector()).isNotNull();

    TablePosition tablePosition = builder.getTableSelector().selectTable("q1");
    assertThat(tablePosition).isNotNull();

    String fileidOut[] = new String[]{null};

    builder = builder.setTableSelector(fileId -> {
      fileidOut[0] = fileId;
      return new TablePosition(19919, 2311);
    });

    assertThat(builder.getTableSelector().selectTable("asd qq oo ii"))
      .isEqualsToByComparingFields(new TablePosition(19919, 2311));
    assertThat(fileidOut[0]).isEqualTo("asd qq oo ii");
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void store_read_parallelCreation(DbType dbType) throws Exception {
    List<DataSource> dataSourceList = dataSourceList(dbType, 3);

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .setIdGenerator(17, () -> "1-2-" + RND.intStr(10))
      .inMultiDb(dataSourceList)
      .setTableIndexLength(7)
      .setTableCountPerDb(5)
      .setTableName("tn_" + RND.intStr(10))
      .setTableSelector(fileId -> {
        String[] split = fileId.split("-");
        return new TablePosition(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
      })
      .build();

    String dataArray[] = new String[3];
    String fileIdArray[] = new String[dataArray.length];

    List<Thread> threadList = new ArrayList<>();
    for (int i = 0; i < dataArray.length; i++) {
      int I = i;
      threadList.add(new Thread(() -> {
        dataArray[I] = "Содержимое " + RND.str(500);
        fileIdArray[I] = fileStorage.storing()
          .name("Hello" + I)
          .data(dataArray[I].getBytes(StandardCharsets.UTF_8))
          .store();
      }));
    }

    threadList.forEach(Thread::start);
    for (Thread thread : threadList) {
      thread.join();
    }

    for (int i = 0; i < dataArray.length; i++) {

      FileDataReader reader = fileStorage.read(fileIdArray[i]);
      assertThat(reader.id()).isEqualTo(fileIdArray[i]);
      assertThat(reader.id()).startsWith("1-2-");
      assertThat(new String(reader.dataAsArray(), StandardCharsets.UTF_8)).startsWith(dataArray[i]);

    }
  }
}