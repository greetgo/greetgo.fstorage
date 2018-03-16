package kz.greetgo.file_storage.impl;

import kz.greetgo.db.DbType;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.impl.util.TestUtil;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Examples {
  public static void main(String[] args) {
    System.out.println();
    new Examples().execWithMonoDb();
    System.out.println();
    new Examples().execWithMultiDb();
  }

  private void execWithMonoDb() {
    DataSource dataSource = TestUtil.createFrom(DbType.Postgres, "fs2");

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .configureFrom(MimeTypeBaseConfigurator.get())
      .mandatoryMimeType(true)
      .mandatoryName(true)
      .inDb(dataSource)
      .build();

    execWith(fileStorage);
  }

  private void execWithMultiDb() {
    DataSource dataSource1 = TestUtil.createFrom(DbType.Postgres, "fs2_1");
    DataSource dataSource2 = TestUtil.createFrom(DbType.Postgres, "fs2_2");
    DataSource dataSource3 = TestUtil.createFrom(DbType.Postgres, "fs2_3");

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .configureFrom(MimeTypeBaseConfigurator.get())
      .mandatoryMimeType(true)
      .mandatoryName(true)
      .inMultiDb(Arrays.asList(dataSource1, dataSource2, dataSource3))
      .build();

    execWith(fileStorage);
  }

  private void execWith(FileStorage fileStorage) {
    String fileId = fileStorage.storing()
      .name("hello.txt")//MimeType вычисляется по расширению имени
      .data("Содержимое текстового файла".getBytes(StandardCharsets.UTF_8))
      .store();

    System.out.println("Hello.txt fileId = " + fileId);

    FileDataReader reader = fileStorage.read(fileId);
    System.out.println("name      = "+reader.name());
    System.out.println("Mime type = "+reader.mimeType());
    System.out.println("createdAt = "+reader.createdAt());
  }

}
