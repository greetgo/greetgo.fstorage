# greetgo.fstorage

File Storage using interfaces:

```java

/**
 * File Storage interface
 */
public interface FileStorage {

  /**
   * Creates new operation for storing new file
   *
   * @return operation for storing new file
   */
  FileStoringOperation storing();

  /**
   * Reads file by id
   *
   * @param fileId reading file id
   * @return file data reader
   * @throws NoFileWithId throws when file with specified id is absent
   */
  FileDataReader read(String fileId) throws NoFileWithId;

  /**
   * Reads file by id without throwing
   *
   * @param fileId reading file id
   * @return file data reader or <code>null</code>, if specified file id is absent
   */
  FileDataReader readOrNull(String fileId);
}


/**
 * File storing operation
 */
public interface FileStoringOperation {
  /**
   * Specifies file name
   *
   * @param name file name
   * @return reference to this
   */
  FileStoringOperation name(String name);

  /**
   * Defines file last modification time
   *
   * @param lastModifiedAt last modification time
   * @return reference to this
   */
  FileStoringOperation lastModifiedAt(Date lastModifiedAt);

  /**
   * Specifies file mime type
   *
   * @param mimeType mime type
   * @return reference to this
   */
  FileStoringOperation mimeType(String mimeType);

  /**
   * Defines file content
   *
   * @param data file content
   * @return reference to this
   */
  FileStoringOperation data(byte[] data);

  /**
   * Defines file content using InputStream
   *
   * @param inputStream input stream for reading file content
   * @return reference to this
   */
  FileStoringOperation data(InputStream inputStream);

  /**
   * Reset file id. This id must be absent in DB.
   * If file with this id already exists, then throws exception, when method {@link #store()} would be called.
   *
   * @param presetFileId preset file id
   * @return reference to this
   */
  FileStoringOperation presetId(String presetFileId);

  /**
   * Run operation
   *
   * @return stored file id
   */
  String store();
}


/**
 * Reader of data from file
 */
public interface FileDataReader {
  /**
   * Reads file name
   *
   * @return file name
   */
  String name();

  /**
   * Reads file content as byte array
   *
   * @return file content as byte array
   */
  byte[] dataAsArray();

  /**
   * Reads file creation date-time
   *
   * @return file creation date-time
   */
  Date createdAt();

  /**
   * Reads file mime type
   *
   * @return file mime type
   */
  String mimeType();

  /**
   * Reads file id
   *
   * @return file id
   */
  String id();
}

```
