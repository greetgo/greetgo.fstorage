package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.*;
import kz.greetgo.file_storage.impl.util.RND;
import kz.greetgo.file_storage.impl.util.TestUtil;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderMonoDbTest extends DataProvidersForTests {

  public static final String SCHEMA = "fs2";

  @Override
  protected boolean traceSql() {
    return false;
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void store_read(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    String data = "Содержимое " + RND.str(400);
    String name = RND.str(10);
    Date lastModifiedAt = RND.dateDays(-10000, -10);

    //
    //
    String fileId = storage.storing()
      .name(name)
      .createdAt(lastModifiedAt)
      .data(data.getBytes(StandardCharsets.UTF_8))
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isNotNull();
    assertThat(reader).isNotNull();
    assertThat(new String(reader.dataAsArray(), StandardCharsets.UTF_8)).isEqualTo(data);
    assertThat(reader.name()).isEqualTo(name);
    assertThat(reader.createdAt()).isEqualTo(lastModifiedAt);
    assertThat(reader.id()).isEqualTo(fileId);

    String name2 = RND.str(10);

    //
    //
    //
    String fileId2 = storage.storing()
      .name(name2)
      .data(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)))
      .store();
    FileDataReader reader2 = storage.read(fileId2);
    //
    //
    //

    assertThat(fileId2).isNotNull();
    assertThat(reader2).isNotNull();
    assertThat(new String(reader2.dataAsArray(), StandardCharsets.UTF_8)).isEqualTo(data);
    assertThat(reader2.name()).isEqualTo(name2);
    assertThat(reader2.createdAt()).isNotNull();
    assertThat(reader2.id()).isEqualTo(fileId2);

    assertThat(fileId).isNotEqualTo(fileId2);
  }

  @Test(expectedExceptions = NoFileWithId.class, dataProvider = "dbTypeDataProvider")
  public void noId_immediately(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    //
    //
    storage.read(RND.str(10));
    //
    //

  }

  @Test(expectedExceptions = NoFileWithId.class, dataProvider = "dbTypeDataProvider")
  public void noId(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    //
    //
    storage.read(RND.str(10)).dataAsArray();
    //
    //

  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void readOrNull_noId_null(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    //
    //
    FileDataReader dataReader = storage.readOrNull(RND.str(10));
    //
    //

    assertThat(dataReader).isNull();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void externalIdGenerator(DbType dbType) {
    String prefix = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .setIdGenerator(20, () -> prefix + RND.str(10))
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    //
    //
    String fileId = storage.storing()
      .name(RND.str(10))
      .data(RND.str(4).getBytes(StandardCharsets.UTF_8))
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isNotNull();
    assertThat(reader).isNotNull();
    assertThat(reader.id()).isEqualTo(fileId);
    assertThat(fileId).startsWith(prefix);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void bigExternalId(DbType dbType) {
    String prefix = RND.str(100);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .setIdGenerator(200, () -> prefix + RND.str(100))
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("idLen200_data")
      .setDataTableId("file_id")
      .setDataTableData("file_data")
      .setParamsTable("idLen200_param")
      .setParamsTableId("param_id")
      .setParamsTableName("param_name")
      .setParamsTableNameLength(250)
      .setParamsTableDataId("param_data_id")
      .setParamsTableMimeType("param_mt")
      .setParamsTableMimeTypeLength(450)
      .setParamsTableLastModifiedAt("updatedAt")

      .build();

    //
    //
    String fileId = storage.storing()
      .name(RND.str(10))
      .data(RND.str(4).getBytes(StandardCharsets.UTF_8))
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isNotNull();
    assertThat(reader).isNotNull();
    assertThat(reader.id()).isEqualTo(fileId);
    assertThat(fileId).startsWith(prefix);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void store_read_presetId(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    String data = "Содержимое " + RND.str(400);
    String name = RND.str(10);
    Date lastModifiedAt = RND.dateDays(-10000, -10);

    String expectedFileId = RND.str(10);

    //
    //
    String fileId = storage.storing()
      .name(name)
      .createdAt(lastModifiedAt)
      .data(data.getBytes(StandardCharsets.UTF_8))
      .presetId(expectedFileId)
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isEqualTo(expectedFileId);
    assertThat(reader).isNotNull();
    assertThat(new String(reader.dataAsArray(), StandardCharsets.UTF_8)).isEqualTo(data);
    assertThat(reader.name()).isEqualTo(name);
    assertThat(reader.createdAt()).isEqualTo(lastModifiedAt);
    assertThat(reader.id()).isEqualTo(fileId);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void testFileIdAlreadyExists(DbType dbType) {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .build();

    String fileId = storage.storing()
      .name(RND.str(10))
      .data(RND.str(10).getBytes(StandardCharsets.UTF_8))
      .store();

    try {
      //
      //
      storage.storing()
        .name(RND.str(10))
        .data(RND.str(10).getBytes(StandardCharsets.UTF_8))
        .presetId(fileId)
        .store();
      //
      //

      Assertions.fail("Cannot add some files with same id: fileId = " + fileId);
    } catch (FileIdAlreadyExists e) {
      assertThat(e.fileId).isEqualTo(fileId);
    }
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkMimeTypeWithCustomLength(DbType dbType) {
    String rnd = RND.intStr(7);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("usingMimeType_data_" + rnd)
      .setParamsTable("usingMimeType_param_" + rnd)
      .setParamsTableMimeType("asd_mime_type_" + rnd)
      .setParamsTableMimeTypeLength(1000)
      .build();

    String mimeType = RND.str(1000);

    //
    //
    String fileId = storage.storing()
      .name(RND.str(10))
      .mimeType(mimeType)
      .data(RND.str(4).getBytes(StandardCharsets.UTF_8))
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isNotNull();
    assertThat(reader).isNotNull();
    assertThat(reader.id()).isEqualTo(fileId);
    assertThat(reader.mimeType()).isEqualTo(mimeType);
  }

  @DataProvider
  public Object[][] nullAndEmpty() {

    List<Object[]> ret = new ArrayList<>();
    for (DbType dbType : dbTypes()) {
      ret.add(new Object[]{dbType, null});
      ret.add(new Object[]{dbType, ""});
    }
    return ret.toArray(new Object[ret.size()][]);
  }

  @Test(expectedExceptions = NoFileMimeType.class, dataProvider = "nullAndEmpty")
  public void checkMimeTypeMandatory(DbType dbType, String nullAndEmpty) {
    String rnd = RND.intStr(7);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mandatoryMimeType(true)
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileMimeType_data_" + rnd)
      .setParamsTable("NoFileMimeType_param_" + rnd)
      .setParamsTableMimeType("asd_mime_type_" + rnd)
      .build();

    //
    //
    storage.storing()
      .mimeType(nullAndEmpty)
      .data(RND.str(4).getBytes(StandardCharsets.UTF_8))
      .store();
    //
    //
  }

  @Test(expectedExceptions = NoFileName.class, dataProvider = "nullAndEmpty")
  public void checkNameMandatory(DbType dbType, String nullAndEmpty) {
    String rnd = RND.intStr(7);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mandatoryName(true)
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileName_data_" + rnd)
      .setParamsTable("NoFileName_param_" + rnd)
      .build();

    //
    //
    storage.storing()
      .name(nullAndEmpty)
      .data(RND.str(4).getBytes(StandardCharsets.UTF_8))
      .store();
    //
    //
  }

  @Test(expectedExceptions = Ora00972_IdentifierIsTooLong.class)
  public void throw_Ora00972_IdentifierIsTooLong_setDataTable() {
    String rnd = RND.intStr(17);

    FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.Oracle, SCHEMA))
      .setDataTable("name_is_very_very_very_long_" + rnd)
      .build();
  }

  @Test(expectedExceptions = Ora00972_IdentifierIsTooLong.class)
  public void throw_Ora00972_IdentifierIsTooLong_setParamsTableMimeType() {
    String rnd = RND.intStr(17);

    FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.Oracle, SCHEMA))
      .setParamsTableMimeType("name_is_very_very_very_long_" + rnd)
      .build();
  }

  @Test(expectedExceptions = Ora00972_IdentifierIsTooLong.class)
  public void throw_Ora00972_IdentifierIsTooLong_setParamsTable() {
    String rnd = RND.intStr(17);

    FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.Oracle, SCHEMA))
      .setParamsTable("name_is_very_very_very_long_" + rnd)
      .build();
  }

  @Test(expectedExceptions = NoFileData.class, dataProvider = "dbTypeDataProvider")
  public void checkDataMandatory(DbType dbType) {
    String rnd = RND.intStr(7);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileContent_data_" + rnd)
      .setParamsTable("NoFileContent_param_" + rnd)
      .build();

    //
    //
    storage.storing().store();
    //
    //
  }


  @Test(dataProvider = "dbTypeDataProvider")
  public void unknownMimeType(DbType dbType) {
    String rnd = RND.intStr(7);

    String[] actualMimeType = new String[]{null};

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        return false;
      })
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileContent_data_" + rnd)
      .setParamsTable("NoFileContent_param_" + rnd)
      .build();

    String mimeType = RND.str(10);

    try {

      //
      //
      storage.storing()
        .mimeType(mimeType)
        .store();
      //
      //

      Assertions.fail("Must to validate MIME type");
    } catch (UnknownMimeType e) {
      assertThat(e.mimeType).isEqualTo(mimeType);
    }

    assertThat(actualMimeType[0]).isEqualTo(mimeType);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void unknownMimeType_throwsSomeError(DbType dbType) {
    String rnd = RND.intStr(7);

    String[] actualMimeType = new String[]{null};
    String errorMessage = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        throw new RuntimeException(errorMessage);
      })
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileContent_data_" + rnd)
      .setParamsTable("NoFileContent_param_" + rnd)
      .build();

    String mimeType = RND.str(10);

    try {

      //
      //
      storage.storing()
        .mimeType(mimeType)
        .store();
      //
      //

      Assertions.fail("Must to validate MIME type");
    } catch (UnknownMimeType e) {
      assertThat(e.mimeType).isEqualTo(mimeType);
      assertThat(e.getCause().getMessage()).isEqualTo(errorMessage);
    }

    assertThat(actualMimeType[0]).isEqualTo(mimeType);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void unknownMimeType_throwsUnknownMimeType(DbType dbType) {
    String rnd = RND.intStr(7);

    String[] actualMimeType = new String[]{null};
    String errorMessage = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        throw new UnknownMimeType(hereMimeType, errorMessage);
      })
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("NoFileContent_data_" + rnd)
      .setParamsTable("NoFileContent_param_" + rnd)
      .build();

    String mimeType = RND.str(10);

    try {

      //
      //
      storage.storing()
        .mimeType(mimeType)
        .store();
      //
      //

      Assertions.fail("Must to validate MIME type");
    } catch (UnknownMimeType e) {
      assertThat(e.mimeType).isEqualTo(mimeType);
      assertThat(e.getMessage()).isEqualTo(errorMessage);
    }

    assertThat(actualMimeType[0]).isEqualTo(mimeType);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_dataTable(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getDataTable()).isEqualTo("file_storage_data");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_dataTableId(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getDataTableId()).isEqualTo("sha1sum");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_dataTableData(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getDataTableData()).isEqualTo("data");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTable(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTable()).isEqualTo("file_storage_params");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableId(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableId()).isEqualTo("id");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableDataId(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableDataId()).isEqualTo("sha1sum");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableLastModifiedAt(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableLastModifiedAt()).isEqualTo("lastModifiedAt");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableMimeType(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableMimeType()).isEqualTo("mimeType");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableMimeTypeLength(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableMimeTypeLength()).isEqualTo(50);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableName(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableName()).isEqualTo("name");
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void checkDefaultValue_paramsTableNameLength(DbType dbType) {
    FileStorageBuilderMonoDb builder = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA));

    assertThat(builder.getParamsTableNameLength()).isEqualTo(300);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void deleteFilesInParallel(DbType dbType) throws Throwable {
    String suffix = RND.intStr(10);
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("data_" + suffix)
      .setParamsTable("par_" + suffix)
      .build();

    class DeleteThread extends Thread {

      private final String fileId;
      private final AtomicInteger okCount;
      private final AtomicInteger errCount;

      public DeleteThread(String fileId, AtomicInteger okCount, AtomicInteger errCount) {
        this.fileId = fileId;
        this.okCount = okCount;
        this.errCount = errCount;
      }

      Throwable error = null;

      @Override
      public void run() {
        try {
          fileStorage.delete(fileId);
          okCount.incrementAndGet();
        } catch (Throwable error) {
          if (!(error instanceof NoFileWithId)) {
            this.error = error;
          }

          errCount.incrementAndGet();
        }
      }
    }

    List<String> fileIdList = new ArrayList<>();

    for (int i = 0; i < 30; i++) {

      String fileId = fileStorage.storing()
        .data(("for deletion" + RND.str(1000)).getBytes(UTF_8))
        .name("File # " + i)
        .store();

      fileIdList.add(fileId);
    }

    for (String fileId : fileIdList) {

      AtomicInteger okCount = new AtomicInteger(0);
      AtomicInteger errCount = new AtomicInteger(0);

      DeleteThread[] threads = new DeleteThread[4];

      for (int i = 0; i < threads.length; i++) {
        threads[i] = new DeleteThread(fileId, okCount, errCount);
      }
      for (DeleteThread thread : threads) {
        thread.start();
      }
      for (DeleteThread thread : threads) {
        thread.join();
        if (thread.error != null) {
          thread.error.printStackTrace();
        }
      }
      for (DeleteThread thread : threads) {
        if (thread.error != null) {
          throw thread.error;
        }
      }

      assertThat(okCount.get()).isEqualTo(1);
      assertThat(errCount.get()).isEqualTo(3);
    }

  }

  @Test(dataProvider = "dbTypeDataProvider", expectedExceptions = NoFileWithId.class)
  public void deleteAbsentFile(DbType dbType) {
    String suffix = RND.intStr(10);
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(dbType, SCHEMA))
      .setDataTable("data1_" + suffix)
      .setParamsTable("par1_" + suffix)
      .build();


    fileStorage.delete(RND.str(10));
  }
}
