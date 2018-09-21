package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileData;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.TableIsAbsent;
import kz.greetgo.file_storage.impl.jdbc.insert.Insert;
import kz.greetgo.file_storage.impl.jdbc.structure.Field;
import kz.greetgo.file_storage.impl.jdbc.structure.FieldType;
import kz.greetgo.file_storage.impl.jdbc.structure.Table;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Date;
import java.util.function.Function;

import static kz.greetgo.file_storage.impl.LocalUtil.readAll;

public class FileStorageMultiDbLogic implements FileStorage {
  private final FileStorageBuilderImpl parent;
  private final FileStorageBuilderMultiDbImpl builder;
  private final MultiDbOperations operations;

  public FileStorageMultiDbLogic(FileStorageBuilderImpl parent,
                                 FileStorageBuilderMultiDbImpl builder,
                                 MultiDbOperations operations) {
    this.parent = parent;
    this.builder = builder;
    this.operations = operations;
  }

  @Override
  public FileStoringOperation storing() {
    return new FileStoringOperation() {
      final CreateNewParams params = new CreateNewParams();

      @Override
      public FileStoringOperation name(String name) {
        params.name = name;

        {
          Function<String, String> mimeTypeExtractor = parent.mimeTypeExtractor;
          if (mimeTypeExtractor != null) {
            params.mimeType = mimeTypeExtractor.apply(name);
          }
        }

        return this;
      }

      @Override
      public FileStoringOperation createdAt(Date createdAt) {
        params.createdAt = createdAt;
        return this;
      }

      @Override
      public FileStoringOperation mimeType(String mimeType) {
        params.mimeType = mimeType;
        return this;
      }

      byte[] data = null;
      InputStream inputStream = null;

      @Override
      public FileStoringOperation data(byte[] data) {
        checkSetData();
        this.data = data == null ? new byte[0] : data;
        return this;
      }

      private void checkSetData() {
        if (data != null || inputStream != null) throw new IllegalStateException("data already defined");
      }

      @Override
      public FileStoringOperation data(InputStream inputStream) {
        if (inputStream == null) throw new IllegalArgumentException("inputStream == null");
        checkSetData();
        this.inputStream = inputStream;
        return this;
      }

      private byte[] getData() {
        if (data != null) return data;
        if (inputStream != null) return readAll(inputStream);
        throw new NoFileData();
      }

      @Override
      public FileStoringOperation presetId(String presetFileId) {
        params.presetFileId = presetFileId;
        return this;
      }

      @Override
      public String store() {
        builder.parent.checkName(params.name);
        builder.parent.checkMimeType(params.mimeType);

        String fileId = params.presetFileId;
        if (fileId == null) fileId = parent.idGenerator.get();

        try {
          createNew(getData(), params, fileId);
          return fileId;
        } catch (TableIsAbsent e) {
          createTableQuiet(fileId);
          createNew(getData(), params, fileId);
          return fileId;
        }
      }
    };
  }

  @Override
  public FileDataReader read(String fileId) throws NoFileWithId {
    FileDataReader reader = readOrNull(fileId);
    if (reader == null) throw new NoFileWithId(fileId);
    return reader;
  }

  @Override
  public FileDataReader readOrNull(String fileId) {
    if (fileId == null || fileId.length() == 0) throw new IllegalArgumentException("fileId = " + fileId);

    FileParams params = loadFileParams(fileId);

    if (params == null) return null;

    return new FileDataReader() {
      @Override
      public String name() {
        return params.name;
      }

      final Object sync = new Object();
      byte[] data = null;

      @Override
      public byte[] dataAsArray() {

        {
          byte[] data = this.data;
          if (data != null) return data;
        }

        synchronized (sync) {
          {
            byte[] data = this.data;
            if (data != null) return data;
          }

          return data = loadData(fileId);
        }
      }

      @Override
      public Date createdAt() {
        return params.createdAt;
      }

      @Override
      public String mimeType() {
        return params.mimeType;
      }

      @Override
      public String id() {
        return params.id;
      }
    };
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private static final String FIELD_ID = "id";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_CREATED_AT = "created_at";
  private static final String FIELD_MIME_TYPE = "mime_type";
  private static final String FIELD_FILE_CONTENT = "file_content";

  private String tableName(TablePosition tablePosition) {
    return builder.tableName + "_" + LocalUtil.toStrLen(tablePosition.tableIndex, builder.tableIndexLength);
  }

  private void createTableQuiet(String fileId) {
    TablePosition tablePosition = builder.tableSelector.selectTable(fileId);

    Table table = new Table(tableName(tablePosition));

    {
      Field f = table.addField();
      f.primaryKey = true;
      f.type = FieldType.STR;
      f.valueLen = parent.fileIdLength;
      f.name = FIELD_ID;
      f.notNull = true;
    }
    {
      Field f = table.addField();
      f.primaryKey = false;
      f.type = FieldType.STR;
      f.valueLen = 255;
      f.name = FIELD_NAME;
      f.notNull = parent.mandatoryName;
    }
    {
      Field f = table.addField();
      f.primaryKey = false;
      f.type = FieldType.TIMESTAMP;
      f.defaultCurrentTimestamp = true;
      f.name = FIELD_CREATED_AT;
      f.notNull = true;
    }
    {
      Field f = table.addField();
      f.primaryKey = false;
      f.type = FieldType.STR;
      f.name = FIELD_MIME_TYPE;
      f.valueLen = 255;
      f.notNull = parent.mandatoryMimeType;
    }
    {
      Field f = table.addField();
      f.primaryKey = false;
      f.type = FieldType.BLOB;
      f.name = FIELD_FILE_CONTENT;
      f.notNull = false;
    }

    DataSource dataSource = extractDataSource(tablePosition);

    operations.createTableQuiet(dataSource, table);
  }

  private DataSource extractDataSource(TablePosition tablePosition) {
    return builder.dataSourceList.get(tablePosition.dbIndex);
  }

  private void createNew(byte[] data, CreateNewParams params, String fileId) throws TableIsAbsent {
    TablePosition tablePosition = builder.tableSelector.selectTable(fileId);
    DataSource dataSource = extractDataSource(tablePosition);

    Insert insert = new Insert(tableName(tablePosition));
    insert.add(FIELD_ID, fileId);
    insert.add(FIELD_FILE_CONTENT, data);
    if (params.mimeType != null) insert.add(FIELD_MIME_TYPE, params.mimeType);
    if (params.name != null) insert.add(FIELD_NAME, params.name);

    operations.insert(dataSource, insert, tablePosition);
  }

  private byte[] loadData(String fileId) {

    TablePosition tablePosition = builder.tableSelector.selectTable(fileId);
    String tableName = tableName(tablePosition);
    DataSource dataSource = extractDataSource(tablePosition);

    return operations.loadData(dataSource, tableName, FIELD_ID, fileId, FIELD_FILE_CONTENT);
  }

  private FileParams loadFileParams(String fileId) {

    TablePosition tablePosition = builder.tableSelector.selectTable(fileId);
    DataSource dataSource = extractDataSource(tablePosition);
    String tableName = tableName(tablePosition);

    TableFieldNames names = new TableFieldNames();
    names.id = FIELD_ID;
    names.name = FIELD_NAME;
    names.mimeType = FIELD_MIME_TYPE;
    names.createdAt = FIELD_CREATED_AT;

    return operations.loadFileParams(dataSource, tableName, fileId, names);
  }
}
