package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.StorageTypeAlreadySelected;
import kz.greetgo.file_storage.impl.util.RND;
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

  @Test(dataProvider = "fileStorageWithNameMandatory", expectedExceptions = NoFileName.class)
  public void checkNameMandatory(FileStorageCreator getter) {
    FileStorage fileStorage = getter.create();

    String fileId = fileStorage.storing().data(RND.byteArray(5)).store();

    assertThat(fileId).isNotNull();
  }

  @Test(dataProvider = "fileStorageWithNameNotMandatory")
  public void checkNameNotMandatory(FileStorageCreator getter) {
    FileStorage fileStorage = getter.create();

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
}
