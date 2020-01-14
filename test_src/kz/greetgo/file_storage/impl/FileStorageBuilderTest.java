package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.StorageTypeAlreadySelected;
import kz.greetgo.file_storage.errors.UnknownMimeType;
import kz.greetgo.file_storage.impl.util.RND;
import kz.greetgo.file_storage.impl.util.TestStorageBuilder;
import org.fest.assertions.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static kz.greetgo.file_storage.impl.util.TestUtil.createFrom;
import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderTest extends DataProvidersForTests {
  @Override
  protected boolean traceSql() {
    return false;
  }

  @Test(expectedExceptions = StorageTypeAlreadySelected.class)
  public void inDb_inMultiDb_1() {
    createFrom(DbType.Postgres, "fs2");
    FileStorageBuilder builder = FileStorageBuilder.newBuilder();
    builder.mandatoryName(true);
    builder.inDb(createFrom(DbType.Postgres, "fs2"));
    builder.inMultiDb(singletonList(createFrom(DbType.Postgres, "fs2")));
  }

  @Test(expectedExceptions = StorageTypeAlreadySelected.class)
  public void inDb_inMultiDb_2() {
    createFrom(DbType.Postgres, "fs2");
    FileStorageBuilder builder = FileStorageBuilder.newBuilder();
    builder.mandatoryName(true);
    builder.inMultiDb(singletonList(createFrom(DbType.Postgres, "fs2")));
    builder.inDb(createFrom(DbType.Postgres, "fs2"));
  }

  @Test(expectedExceptions = NoFileName.class, dataProvider = "testStorageBuilder_DP")
  public void checkNameMandatory(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.setNameMandatory(true).build();

    String fileId = fileStorage.storing().data(RND.byteArray(5)).store();

    assertThat(fileId).isNotNull();
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void checkNameNotMandatory(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.build();

    String content = "Привет " + RND.str(500);
    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .store();

    assertThat(fileId).isNotNull();
    assertThat(fileId).isNotEmpty();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader).isNotNull();
    assertThat(reader.id()).isEqualTo(fileId);
    assertThat(new String(reader.dataAsArray(), UTF_8)).isEqualTo(content);
    if (!builder.isMongoGridFs()) {
      assertThat(reader.name()).isNull();
    }
  }

  @Test(expectedExceptions = NoFileMimeType.class, dataProvider = "testStorageBuilder_DP")
  public void checkMimeTypeMandatory(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.setMimeTypeMandatory(true).build();

    fileStorage.storing().data(RND.byteArray(5)).store();
  }

  @Test(dataProvider = "testStorageBuilder_DP", expectedExceptions = UnknownMimeType.class)
  public void checkMimeTypeValidator_hasLeftMimeType(TestStorageBuilder builder) {
    FileStorage fileStorage = builder
      .setMimeTypeValidator(mimeType -> false)
      .build();

    fileStorage.storing().data(RND.byteArray(5)).mimeType("wow").store();
  }

  @Test(dataProvider = "testStorageBuilder_DP", expectedExceptions = UnknownMimeType.class)
  public void checkMimeTypeValidator_noMimeType(TestStorageBuilder builder) {
    FileStorage fileStorage = builder
      .setMimeTypeValidator(mimeType -> false)
      .build();

    fileStorage.storing().mimeType("some").data(RND.byteArray(5)).store();
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void checkMimeTypeValidator_validatorThrows(TestStorageBuilder builder) {
    FileStorage fileStorage = builder
      .setMimeTypeValidator(mimeType -> {throw new RuntimeException("Ka ra bas");})
      .build();

    try {
      fileStorage.storing().data(RND.byteArray(5)).mimeType("wow").store();
      Assertions.fail("validator does not works");
    } catch (UnknownMimeType e) {
      assertThat(e.getCause().getMessage()).isEqualTo("Ka ra bas");
    }
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void checkMimeTypeExtractor(TestStorageBuilder builder) {
    FileStorage fileStorage = builder
      .setMimeTypeExtractor(fileName -> fileName.substring(0, 5))
      .build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10);

    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isEqualTo(name.substring(0, 5));
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void probeMimeTypeBaseConfigurator_existsExtension(TestStorageBuilder builder) {
    MimeTypeConfigurator mimeTypeConfigurator = new MimeTypeConfigurator();
    mimeTypeConfigurator.registerBaseMimeTypes();
    mimeTypeConfigurator.configure(builder.getBuilder());

    FileStorage fileStorage = builder.build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10) + ".txt";

    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isEqualTo("text/plain");
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void probeMimeTypeBaseConfigurator_leftExtension(TestStorageBuilder builder) {
    MimeTypeConfigurator mimeTypeConfigurator = new MimeTypeConfigurator();
    mimeTypeConfigurator.registerBaseMimeTypes();
    mimeTypeConfigurator.configure(builder.getBuilder());

    FileStorage fileStorage = builder.build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10) + ".left_extension";

    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isNull();
  }

  @Test(dataProvider = "testStorageBuilder_DP", expectedExceptions = NoFileWithId.class)
  public void deleteFile_NoFileWithId(TestStorageBuilder builder) {

    if (builder.isMongoGridFs()) {
      throw new SkipException("NoFileWithId is unsupported for MongoGridFs");
    }

    builder.build().delete(RND.str(10));
  }


  @Test(dataProvider = "testStorageBuilder_DP")
  public void deleteFile_ok(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.build();

    String fileId = fileStorage.storing()
      .name(RND.str(10) + ".txt")
      .data(RND.byteArray(1000))
      .store();

    assertThat(fileStorage.readOrNull(fileId)).isNotNull();

    //
    //
    fileStorage.delete(fileId);
    //
    //

    assertThat(fileStorage.readOrNull(fileId)).isNull();
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void sameFileName(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.build();

    String fileName = RND.str(10);

    String content1 = "Привет " + RND.str(10);
    String content2 = "Привет " + RND.str(10);

    String fileId1 = fileStorage.storing()
      .data(content1.getBytes(UTF_8))
      .name(fileName)
      .store();

    String fileId2 = fileStorage.storing()
      .data(content2.getBytes(UTF_8))
      .name(fileName)
      .store();

    FileDataReader reader1 = fileStorage.read(fileId1);
    FileDataReader reader2 = fileStorage.read(fileId2);

    String actual1 = new String(reader1.dataAsArray(), UTF_8);
    String actual2 = new String(reader2.dataAsArray(), UTF_8);

    assertThat(actual1).isEqualTo(content1);
    assertThat(actual2).isEqualTo(content2);
    assertThat(reader1.name()).isEqualTo(fileName);
    assertThat(reader2.name()).isEqualTo(fileName);
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void store_and_read_using_dataAsArray(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.build();

    String fileName = RND.str(10);
    String content = RND.str(100);

    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .name(fileName)
      .store();

    byte[] bytes = fileStorage.read(fileId).dataAsArray();

    String actual = new String(bytes, UTF_8);

    assertThat(actual).isEqualTo(content);

  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void store_and_read_using_writeTo(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.build();

    String fileName = RND.str(10);
    String content = RND.str(100);

    String fileId = fileStorage.storing()
      .data(content.getBytes(UTF_8))
      .name(fileName)
      .store();

    ByteArrayOutputStream bOut = new ByteArrayOutputStream();

    fileStorage.read(fileId).writeTo(bOut);

    String actual = new String(bOut.toByteArray(), UTF_8);

    assertThat(actual).isEqualTo(content);

  }

}
