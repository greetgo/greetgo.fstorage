package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.errors.MultipleBuilderUsage;
import kz.greetgo.file_storage.errors.StorageTypeAlreadySelected;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static kz.greetgo.file_storage.impl.util.TestUtil.createFrom;

public class FileStorageBuilderTest {

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
}