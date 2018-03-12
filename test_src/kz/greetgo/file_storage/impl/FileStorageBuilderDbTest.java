package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.FileIdAlreadyExists;
import kz.greetgo.file_storage.errors.NoFileData;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.UnknownMimeType;
import kz.greetgo.util.db.DbType;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderDbTest {

  @BeforeMethod
  public void setNewSqlLogger_to_FileStorageLogger() throws Exception {
    FileStorageLogger.setNewSqlLogger(new FileStorageLogger.SqlLogger() {
      @Override
      public boolean isTraceEnabled() {
        return true;
      }

      @Override
      public void traceSQL(String sql) {
        System.out.println(sql);
      }

      @Override
      public void traceSqlParam(int paramIndex, Object paramValue) {
        System.out.println("    paramIndex = " + paramIndex + ", paramValue = " + paramValue);
      }
    });
  }

  @Test
  public void store_read() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .build();

    String data = "Содержимое " + RND.str(400);
    String name = RND.str(10);
    Date lastModifiedAt = RND.dateDays(-10000, -10);

    //
    //
    String fileId = storage.storing()
      .name(name)
      .lastModifiedAt(lastModifiedAt)
      .data(data.getBytes(StandardCharsets.UTF_8))
      .store();
    FileDataReader reader = storage.read(fileId);
    //
    //

    assertThat(fileId).isNotNull();
    assertThat(reader).isNotNull();
    assertThat(new String(reader.dataAsArray(), StandardCharsets.UTF_8)).isEqualTo(data);
    assertThat(reader.name()).isEqualTo(name);
    assertThat(reader.lastModifiedAt()).isEqualTo(lastModifiedAt);
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
    assertThat(reader2.lastModifiedAt()).isNotNull();
    assertThat(reader2.id()).isEqualTo(fileId2);

    assertThat(fileId).isNotEqualTo(fileId2);
  }

  @Test(expectedExceptions = NoFileWithId.class)
  public void noId_immediately() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .build();

    //
    //
    storage.read(RND.str(10));
    //
    //

  }

  @Test(expectedExceptions = NoFileWithId.class)
  public void noId() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .build();

    //
    //
    storage.read(RND.str(10)).dataAsArray();
    //
    //

  }

  @Test
  public void readOrNull_noId_null() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .build();

    //
    //
    FileDataReader dataReader = storage.readOrNull(RND.str(10));
    //
    //

    assertThat(dataReader).isNull();
  }

  @Test
  public void externalIdGenerator() throws Exception {
    String prefix = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .idGenerator(20, () -> prefix + RND.str(10))
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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

  @Test
  public void bigExternalId() throws Exception {
    String prefix = RND.str(100);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .idGenerator(200, () -> prefix + RND.str(100))
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .setDataTable("idLen200_data")
      .setParamsTable("idLen200_param")
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

  @Test
  public void store_read_presetId() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .build();

    String data = "Содержимое " + RND.str(400);
    String name = RND.str(10);
    Date lastModifiedAt = RND.dateDays(-10000, -10);

    String expectedFileId = RND.str(10);

    //
    //
    String fileId = storage.storing()
      .name(name)
      .lastModifiedAt(lastModifiedAt)
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
    assertThat(reader.lastModifiedAt()).isEqualTo(lastModifiedAt);
    assertThat(reader.id()).isEqualTo(fileId);
  }

  @Test
  public void testFileIdAlreadyExists() throws Exception {

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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

  @Test
  public void checkMimeTypeWithCustomLength() throws Exception {
    String rnd = RND.intStr(17);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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
    return new Object[][]{{null}, {""}};
  }

  @Test(expectedExceptions = NoFileMimeType.class, dataProvider = "nullAndEmpty")
  public void checkMimeTypeMandatory(String nullAndEmpty) throws Exception {
    String rnd = RND.intStr(17);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mandatoryMimeType(true)
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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
  public void checkNameMandatory(String nullAndEmpty) throws Exception {
    String rnd = RND.intStr(17);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mandatoryName(true)
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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

  @Test(expectedExceptions = NoFileData.class)
  public void checkDataMandatory() throws Exception {
    String rnd = RND.intStr(17);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
      .setDataTable("NoFileContent_data_" + rnd)
      .setParamsTable("NoFileContent_param_" + rnd)
      .build();

    //
    //
    storage.storing().store();
    //
    //
  }


  @Test
  public void unknownMimeType() throws Exception {
    String rnd = RND.intStr(17);

    String actualMimeType[] = new String[]{null};

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        return false;
      })
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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

  @Test
  public void unknownMimeType_throwsSomeError() throws Exception {
    String rnd = RND.intStr(17);

    String actualMimeType[] = new String[]{null};
    String errorMessage = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        throw new RuntimeException(errorMessage);
      })
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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

  @Test
  public void unknownMimeType_throwsUnknownMimeType() throws Exception {
    String rnd = RND.intStr(17);

    String actualMimeType[] = new String[]{null};
    String errorMessage = RND.str(10);

    FileStorage storage = FileStorageBuilder
      .newBuilder()
      .mimeTypeValidator(hereMimeType -> {
        actualMimeType[0] = hereMimeType;
        throw new UnknownMimeType(hereMimeType, errorMessage);
      })
      .inDb(TestUtil.createFrom(DbType.PostgreSQL, "_fs2"))
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
}