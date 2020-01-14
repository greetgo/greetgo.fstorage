package kz.greetgo.file_storage.impl;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.NoFileWithId;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static kz.greetgo.file_storage.impl.MongoUtil.toByteArray;
import static kz.greetgo.file_storage.impl.MongoUtil.toDate;
import static kz.greetgo.file_storage.impl.MongoUtil.toStr;

class FileStorageMongodb implements FileStorage {
  private final FileStorageBuilderInMongodbImpl builder;

  FileStorageMongodb(FileStorageBuilderInMongodbImpl builder) {this.builder = builder;}

  @Override
  public FileDataReader read(String fileId) throws NoFileWithId {
    FileDataReader reader = readOrNull(fileId);

    if (reader == null) {
      throw new NoFileWithId(fileId);
    }

    return reader;
  }

  @Override
  public FileDataReader readOrNull(String fileId) {

    final Document record = builder.collection

      .find(eq(builder.names.id, fileId))

      .projection(include(
        builder.names.name,
        builder.names.mimeType,
        builder.names.createdAt
      ))

      .first();

    if (record == null) {
      return null;
    }

    return new FileDataReader() {
      @Override
      public String name() {
        return toStr(record.get(builder.names.name));
      }

      byte[] data = null;

      final Object sync = new Object();

      @Override
      public byte[] dataAsArray() {
        {
          byte[] data = this.data;
          if (data != null) {
            return data;
          }
        }

        synchronized (sync) {
          {
            byte[] data = this.data;
            if (data != null) {
              return data;
            }
          }

          return data = loadData();
        }
      }

      private byte[] loadData() {
        final Document record = builder.collection
          .find(eq(builder.names.id, fileId))
          .projection(include(builder.names.content))
          .first();

        if (record == null) {
          throw new NullPointerException("record == null for fileId = " + fileId);
        }

        return toByteArray(record.get(builder.names.content));
      }

      @Override
      public Date createdAt() {
        return toDate(record.get(builder.names.createdAt));
      }

      @Override
      public String mimeType() {
        return toStr(record.get(builder.names.mimeType));
      }

      @Override
      public String id() {
        return fileId;
      }

      @Override
      public void writeTo(OutputStream out) {
        try {
          out.write(dataAsArray());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Override
  public void delete(String fileId) throws NoFileWithId {
    DeleteResult deleteResult = builder.collection.deleteOne(eq(builder.names.id, fileId));
    if (deleteResult.getDeletedCount() < 1) {
      throw new NoFileWithId(fileId);
    }
  }

  @Override
  public FileStoringOperation storing() {
    return new FileStoringOperation() {
      String name = null;

      @Override
      public FileStoringOperation name(String name) {
        this.name = name;

        Function<String, String> mimeTypeExtractor = builder.parent.mimeTypeExtractor;
        if (mimeTypeExtractor != null) {
          mimeType = mimeTypeExtractor.apply(name);
        }

        return this;
      }

      String name() {
        if (builder.parent.mandatoryName && name == null) {
          throw new NoFileName();
        }
        return name;
      }

      Date createdAt = null;

      @Override
      public FileStoringOperation createdAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
      }

      String mimeType = null;

      @Override
      public FileStoringOperation mimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
      }

      String mimeType() {
        return builder.parent.validateMimeType(mimeType);
      }

      byte[] data = null;

      @Override
      public FileStoringOperation data(byte[] data) {
        Objects.requireNonNull(data);
        this.data = data;
        inputStream = null;
        return this;
      }

      InputStream inputStream = null;

      @Override
      public FileStoringOperation data(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        data = null;
        this.inputStream = inputStream;
        return this;
      }

      byte[] data() {
        if (inputStream != null) {
          return LocalUtil.readAll(inputStream);
        }

        if (data != null) {
          return data;
        }

        throw new RuntimeException("No data to insert");
      }

      private String presetFileId = null;

      @Override
      public FileStoringOperation presetId(String presetFileId) {
        this.presetFileId = presetFileId;
        return this;
      }

      @Override
      public String store() {
        ensureIndex();

        String id = presetFileId;
        if (id == null) {
          id = builder.parent.idGenerator(IdGeneratorType.STR13).get();
        }

        Date createdAt = this.createdAt;
        if (createdAt == null) {
          createdAt = new Date();
        }

        Document insert = new Document();
        insert.append(builder.names.id, id);
        insert.append(builder.names.name, name());
        insert.append(builder.names.mimeType, mimeType());
        insert.append(builder.names.content, data());
        insert.append(builder.names.createdAt, createdAt);

        builder.collection.insertOne(insert);

        return id;
      }
    };
  }

  private final AtomicBoolean ensureIndexWasCalled = new AtomicBoolean(false);

  private void ensureIndex() {
    if (ensureIndexWasCalled.get()) {
      return;
    }
    ensureIndexWasCalled.set(true);

    IndexOptions options = new IndexOptions();
    options.unique(true);

    Document index = new Document();
    index.append(builder.names.id, 1);

    builder.collection.createIndex(index, options);
  }
}
