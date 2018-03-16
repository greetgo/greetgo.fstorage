package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.StorageTypeAlreadySelected;
import kz.greetgo.file_storage.errors.UnknownMimeType;
import kz.greetgo.file_storage.impl.util.RND;
import kz.greetgo.file_storage.impl.util.TestStorageBuilder;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static kz.greetgo.file_storage.impl.util.TestUtil.createFrom;
import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageBuilderTest extends DataProvidersForTests {
  @Override
  protected boolean traceSql() {
    return false;
  }

  @Test(expectedExceptions = StorageTypeAlreadySelected.class)
  public void inDb_inMultiDb_1() throws Exception {
    createFrom(DbType.Postgres, "fs2");
    FileStorageBuilder builder = FileStorageBuilder.newBuilder();
    builder.mandatoryName(true);
    builder.inDb(createFrom(DbType.Postgres, "fs2"));
    builder.inMultiDb(singletonList(createFrom(DbType.Postgres, "fs2")));
  }

  @Test(expectedExceptions = StorageTypeAlreadySelected.class)
  public void inDb_inMultiDb_2() throws Exception {
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
      .data(content.getBytes(StandardCharsets.UTF_8))
      .store();

    assertThat(fileId).isNotNull();
    assertThat(fileId).isNotEmpty();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader).isNotNull();
    assertThat(reader.id()).isEqualTo(fileId);
    assertThat(new String(reader.dataAsArray(), StandardCharsets.UTF_8)).isEqualTo(content);
    assertThat(reader.name()).isNull();
  }

  @Test(expectedExceptions = NoFileMimeType.class, dataProvider = "testStorageBuilder_DP")
  public void checkMimeTypeMandatory(TestStorageBuilder builder) {
    FileStorage fileStorage = builder.setMimeTypeMandatory(true).build();

    fileStorage.storing().data(RND.byteArray(5)).store();
  }

  @Test(dataProvider = "testStorageBuilder_DP", expectedExceptions = UnknownMimeType.class)
  public void checkMimeTypeValidator_hasLeftMimeType(TestStorageBuilder builder) throws Exception {
    FileStorage fileStorage = builder
      .setMimeTypeValidator(mimeType -> false)
      .build();

    fileStorage.storing().data(RND.byteArray(5)).mimeType("wow").store();
  }

  @Test(dataProvider = "testStorageBuilder_DP", expectedExceptions = UnknownMimeType.class)
  public void checkMimeTypeValidator_noMimeType(TestStorageBuilder builder) throws Exception {
    FileStorage fileStorage = builder
      .setMimeTypeValidator(mimeType -> false)
      .build();

    fileStorage.storing().data(RND.byteArray(5)).store();
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void checkMimeTypeValidator_validatorThrows(TestStorageBuilder builder) throws Exception {
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
  public void checkMimeTypeExtractor(TestStorageBuilder builder) throws Exception {
    FileStorage fileStorage = builder
      .setMimeTypeExtractor(fileName -> fileName.substring(0, 5))
      .build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10);

    String fileId = fileStorage.storing()
      .data(content.getBytes(StandardCharsets.UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isEqualTo(name.substring(0, 5));
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void probeMimeTypeBaseConfigurator_existsExtension(TestStorageBuilder builder) throws Exception {
    MimeTypeBaseConfigurator.get().configure(builder.getBuilder());

    FileStorage fileStorage = builder.build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10) + ".txt";

    String fileId = fileStorage.storing()
      .data(content.getBytes(StandardCharsets.UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isEqualTo("text/plain");
  }

  @Test(dataProvider = "testStorageBuilder_DP")
  public void probeMimeTypeBaseConfigurator_leftExtension(TestStorageBuilder builder) throws Exception {
    MimeTypeBaseConfigurator.get().configure(builder.getBuilder());

    FileStorage fileStorage = builder.build();

    String content = "Привет " + RND.str(10);
    String name = RND.str(10) + ".left_extension";

    String fileId = fileStorage.storing()
      .data(content.getBytes(StandardCharsets.UTF_8))
      .name(name)
      .store();

    FileDataReader reader = fileStorage.read(fileId);
    assertThat(reader.mimeType()).isNull();
  }
}
