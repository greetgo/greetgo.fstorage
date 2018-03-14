package kz.greetgo.file_storage.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public abstract class AbstractStorageMonoDbDao implements StorageMonoDbDao {
  protected final FileStorageBuilderMonoDbImpl builder;

  protected AbstractStorageMonoDbDao(FileStorageBuilderMonoDbImpl builder) {this.builder = builder;}

  private static final String PREFIX = "__";
  private static final String SUFFIX = "__";

  protected String sql(String sql) {
    return sql
      .replaceAll(PREFIX + "dataTable" + SUFFIX, builder.dataTable)
      .replaceAll(PREFIX + "dataTableId" + SUFFIX, builder.dataTableId)
      .replaceAll(PREFIX + "dataTableData" + SUFFIX, builder.dataTableData)

      .replaceAll(PREFIX + "paramsTable" + SUFFIX, builder.paramsTable)
      .replaceAll(PREFIX + "paramsTableId" + SUFFIX, builder.paramsTableId)
      .replaceAll(PREFIX + "paramsTableName" + SUFFIX, builder.paramsTableName)
      .replaceAll(PREFIX + "paramsTableMimeType" + SUFFIX, builder.paramsTableMimeType)
      .replaceAll(PREFIX + "paramsTableDataId" + SUFFIX, builder.paramsTableDataId)
      .replaceAll(PREFIX + "paramsTableLastModifiedAt" + SUFFIX, builder.paramsTableLastModifiedAt)
      ;
  }

  protected String sha1sum(byte[] data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");

      digest.reset();
      digest.update(data);

      try (Formatter formatter = new Formatter()) {

        for (byte b : digest.digest()) {
          formatter.format("%02x", b);
        }

        return formatter.toString();
      }

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
