package kz.greetgo.file_storage;

import java.io.InputStream;
import java.util.Date;

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
