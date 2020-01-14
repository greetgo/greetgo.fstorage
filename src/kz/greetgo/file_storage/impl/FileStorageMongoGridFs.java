package kz.greetgo.file_storage.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import kz.greetgo.file_storage.FileDataReader;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.FileStoringOperation;
import kz.greetgo.file_storage.errors.NoFileMimeType;
import kz.greetgo.file_storage.errors.NoFileName;
import kz.greetgo.file_storage.errors.NoFileWithId;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;
import static kz.greetgo.file_storage.impl.IdGeneratorType.HEX12;

public class FileStorageMongoGridFs implements FileStorage {
  private final FileStorageBuilderInMongoGridFsImpl builder;
  private final GridFSBucket bucket;

  public FileStorageMongoGridFs(FileStorageBuilderInMongoGridFsImpl builder) {
    this.builder = builder;
    bucket = createBucket();
  }

  private GridFSBucket createBucket() {
    String bucketName = builder.bucketName;
    if (bucketName == null || bucketName.trim().length() == 0) {
      return GridFSBuckets.create(builder.database);
    }
    return GridFSBuckets.create(builder.database, bucketName);
  }

  @Override
  public FileStoringOperation storing() {

    return new FileStoringOperation() {
      String fileName = null;

      @Override
      public FileStoringOperation name(String name) {
        this.fileName = name;

        Function<String, String> mimeTypeExtractor = builder.parent.mimeTypeExtractor;
        if (mimeTypeExtractor != null) {
          mimeType = mimeTypeExtractor.apply(name);
        }

        return this;
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
        builder.parent.validateMimeType(mimeType);
        this.mimeType = mimeType;
        return this;
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
        this.inputStream = inputStream;
        data = null;
        return this;
      }

      String presetFileId = null;

      @Override
      public FileStoringOperation presetId(String presetFileId) {
        this.presetFileId = presetFileId;
        return this;
      }

      @Override
      public String store() {
        return storeFile(fileName, createdAt, mimeType, data, inputStream, presetFileId);
      }
    };
  }

  private String storeFile(String fileName, Date createdAt, String mimeType,
                           byte[] data, InputStream inputStreamArg, String presetFileId) {

    HashMap<String, Object> metadata = new HashMap<>();
    metadata.put("createdAt", createdAt != null ? createdAt : new Date());
    if (mimeType != null) {
      metadata.put("mimeType", mimeType);
    } else if (builder.parent.mandatoryMimeType) {
      throw new NoFileMimeType();
    }

    if (fileName == null && builder.parent.mandatoryName) {
      throw new NoFileName();
    }

    GridFSUploadOptions options = new GridFSUploadOptions()
      .chunkSizeBytes(builder.chinkSizeBytes)
      .metadata(new Document(metadata));

    String id = presetFileId;
    if (id == null || id.trim().length() == 0) {
      id = builder.parent.idGenerator(HEX12).get();
    }

    BsonValue bsonId = convertToBsonId(id);

    InputStream inputStream = inputStreamArg;
    if (inputStream == null) {
      inputStream = new ByteArrayInputStream(Objects.requireNonNull(data, "data - array of bytes - content of file"));
    }

    if (fileName == null) {
      fileName = "default-name";
    }

    bucket.uploadFromStream(bsonId, fileName, inputStream, options);

    return id;
  }

  public static class IllegalId extends RuntimeException {
    public IllegalId(String message) {
      super(message);
    }

    public IllegalId(String message, Throwable e) {
      super(message, e);
    }
  }

  private BsonValue convertToBsonId(String id) {

    if (builder.useObjectId) {

      byte[] idBytes;
      try {
        idBytes = HexUtil.hexToBytes(id);
      } catch (HexUtil.HexConvertException e) {
        throw new IllegalId("When useObjectId == true, then id must be hex string for 12 bytes: " + e.getMessage(), e);
      }

      if (idBytes.length != 12) {
        throw new IllegalId("When useObjectId == true, then id must be hex string for 12 bytes," +
          " but now length = " + idBytes.length + " : id = `" + id + "`");
      }

      ObjectId objectId = new ObjectId(idBytes);

      return new BsonObjectId(objectId);

    } else {

      return new BsonString(id);

    }
  }

  @Override
  public FileDataReader read(String fileId) throws NoFileWithId {
    FileDataReader reader = readOrNull(fileId);

    if (reader == null) {
      throw new NoFileWithId(fileId);
    }

    return reader;
  }

  @Override
  public void delete(String fileId) throws NoFileWithId {
    bucket.delete(convertToBsonId(fileId));
  }

  @Override
  public FileDataReader readOrNull(String fileId) {

    BsonValue bsonId = convertToBsonId(fileId);

    GridFSFindIterable iterable = bucket.find(eq("_id", bsonId));
    GridFSFile file = iterable.first();

    if (file == null) return null;

    return new FileDataReader() {
      @Override
      public String name() {
        return file.getFilename();
      }

      @Override
      public byte[] dataAsArray() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        writeTo(bOut);
        return bOut.toByteArray();
      }

      @Override
      public void writeTo(OutputStream out) {
        bucket.downloadToStream(bsonId, out);
      }

      @Override
      public Date createdAt() {
        Document metadata = file.getMetadata();
        if (metadata == null) {
          return file.getUploadDate();
        }
        Object createdAt = metadata.get("createdAt");
        if (createdAt == null) {
          return file.getUploadDate();
        }
        return (Date) createdAt;
      }

      @Override
      public String mimeType() {
        Document metadata = file.getMetadata();
        if (metadata == null) {
          return null;
        }
        Object mimeType = metadata.get("mimeType");
        if (mimeType == null) {
          return null;
        }
        return (String) mimeType;
      }

      @Override
      public String id() {
        return fileId;
      }
    };
  }
}
