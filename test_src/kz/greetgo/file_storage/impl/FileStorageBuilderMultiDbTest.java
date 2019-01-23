package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.impl.util.RND;
import kz.greetgo.file_storage.impl.util.TestUtil;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderMultiDbTest extends DataProvidersForTests {
  public static final String SCHEMA_PREFIX = "multi_fs";

  @Override
  protected boolean traceSql() {
    return false;
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableIndexLength(DbType dbType) {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, SCHEMA_PREFIX, 3));

    assertThat(builder.getTableIndexLength()).isEqualTo(5);

    builder = builder.setTableIndexLength(7);

    assertThat(builder.getTableIndexLength()).isEqualTo(7);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableName(DbType dbType) {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, SCHEMA_PREFIX, 3));

    assertThat(builder.getTableName()).isEqualTo("file_storage");

    builder = builder.setTableName("left_name");

    assertThat(builder.getTableName()).isEqualTo("left_name");
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableCountPerDb(DbType dbType) {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, SCHEMA_PREFIX, 3));

    assertThat(builder.getTableCountPerDb()).isEqualTo(12);

    builder = builder.setTableCountPerDb(42);

    assertThat(builder.getTableCountPerDb()).isEqualTo(42);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void defaultValue_tableDetector(DbType dbType) {
    FileStorageBuilderMultiDb builder = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList(dbType, SCHEMA_PREFIX, 3));

    assertThat(builder.getTableSelector()).isNotNull();

    TablePosition tablePosition = builder.getTableSelector().selectTable("q1");
    assertThat(tablePosition).isNotNull();

    String[] fileIdOut = new String[]{null};

    builder = builder.setTableSelector(fileId -> {
      fileIdOut[0] = fileId;
      return new TablePosition(19919, 2311);
    });

    assertThat(builder.getTableSelector().selectTable("asd qq oo ii"))
      .isEqualsToByComparingFields(new TablePosition(19919, 2311));
    assertThat(fileIdOut[0]).isEqualTo("asd qq oo ii");
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void store_read_parallelCreation(DbType dbType) throws Exception {
    List<DataSource> dataSourceList = dataSourceList(dbType, SCHEMA_PREFIX, 3);

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .setIdGenerator(17, () -> "1-2-" + RND.intStr(10))
      .inMultiDb(dataSourceList)
      .setTableIndexLength(7)
      .setTableCountPerDb(5)
      .setTableName("tn1_" + RND.intStr(10))
      .setTableSelector(fileId -> {
        String[] split = fileId.split("-");
        return new TablePosition(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
      })
      .build();

    String[] dataArray = new String[7];
    String[] fileIdArray = new String[dataArray.length];

    List<Thread> threadList = new ArrayList<>();
    for (int i = 0; i < dataArray.length; i++) {
      int I = i;
      threadList.add(new Thread(() -> {
        dataArray[I] = "Содержимое " + RND.str(500);
        fileIdArray[I] = fileStorage.storing()
          .name("Hello" + I)
          .data(dataArray[I].getBytes(UTF_8))
          .store();
      }));
    }

    threadList.forEach(Thread::start);
    for (Thread thread : threadList) {
      thread.join();
    }

    for (int i = 0; i < dataArray.length; i++) {

      if (fileIdArray[i] == null) {
        Assertions.fail("fileIdArray[" + i + "] == null");
      }

      FileDataReader reader = fileStorage.read(fileIdArray[i]);
      assertThat(reader.id()).isEqualTo(fileIdArray[i]);
      assertThat(reader.id()).startsWith("1-2-");
      assertThat(new String(reader.dataAsArray(), UTF_8)).startsWith(dataArray[i]);

    }
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void store_read_delete_parallelCreation_manyRecords(DbType dbType) throws Exception {
    List<DataSource> dataSourceList = dataSourceList(dbType, SCHEMA_PREFIX, 4);

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList)
      .setTableCountPerDb(3)
      .setTableName("tn2_" + RND.intStr(10))
      .build();

    class Dot {
      final String name;
      final String content;

      final AtomicReference<String> fileId = new AtomicReference<>(null);

      public Dot(String name, String content) {
        this.name = name;
        this.content = content;
      }

      @Override
      public String toString() {
        return "name = " + name + ", fileId = " + fileId.get();
      }
    }

    List<Dot> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(new Dot("Файл" + i, "Содержание " + RND.str(500)));
    }

    {
      LinkedList<Dot> queue = new LinkedList<>(list);

      List<Thread> threadList = new ArrayList<>();
      for (int i = 0; i < 7; i++) {
        threadList.add(new Thread(() -> {

          while (true) {
            Dot dot;

            synchronized (queue) {
              dot = queue.poll();
            }

            if (dot == null) {
              return;
            }
            dot.fileId.set(fileStorage.storing()
              .name(dot.name)
              .data(dot.content.getBytes(UTF_8))
              .store());
          }

        }));
      }

      threadList.forEach(Thread::start);
      for (Thread thread1 : threadList) {
        thread1.join();
      }
    }

    for (Dot dot : list) {
      FileDataReader reader = fileStorage.read(dot.fileId.get());
      assertThat(reader.id()).isEqualTo(dot.fileId.get());
      assertThat(new String(reader.dataAsArray(), UTF_8)).isEqualTo(dot.content);
      assertThat(reader.name()).isEqualTo(dot.name);
    }

    for (Dot dot : list) {

      fileStorage.delete(dot.fileId.get());

      FileDataReader reader = fileStorage.readOrNull(dot.fileId.get());
      assertThat(reader).isNull();
    }

  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void deleteFilesInParallel(DbType dbType) throws InterruptedException {
    List<DataSource> dataSourceList = dataSourceList(dbType, SCHEMA_PREFIX, 4);

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList)
      .setTableCountPerDb(3)
      .setTableName("del_" + RND.intStr(10))
      .build();

    List<String> fileIdList = new ArrayList<>();

    for (int i = 0; i < 300; i++) {

      String fileId = fileStorage.storing()
        .data(("for deletion" + RND.str(1000)).getBytes(UTF_8))
        .name("File # " + i)
        .store();

      fileIdList.add(fileId);
    }

    for (String fileId : fileIdList) {

      final AtomicInteger ok = new AtomicInteger(0);
      final AtomicInteger error = new AtomicInteger(0);

      Runnable actionDelete = () -> {
        try {
          fileStorage.delete(fileId);
          ok.incrementAndGet();
        } catch (NoFileWithId e) {
          error.incrementAndGet();
        }
      };

      Thread thread1 = new Thread(actionDelete);
      Thread thread2 = new Thread(actionDelete);
      Thread thread3 = new Thread(actionDelete);
      Thread thread4 = new Thread(actionDelete);

      thread1.start();
      thread2.start();
      thread3.start();
      thread4.start();

      thread1.join();
      thread2.join();
      thread3.join();
      thread4.join();

      assertThat(ok.get()).isEqualTo(1);
      assertThat(error.get()).isEqualTo(3);
    }

  }

  @Test(dataProvider = "dbTypeDataProvider", expectedExceptions = NoFileWithId.class)
  public void deleteAbsentFile(DbType dbType) {
    List<DataSource> dataSourceList = dataSourceList(dbType, SCHEMA_PREFIX, 4);

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inMultiDb(dataSourceList)
      .setTableCountPerDb(3)
      .setTableName("del1_" + RND.intStr(10))
      .build();

    fileStorage.delete(RND.str(10));
  }
}
