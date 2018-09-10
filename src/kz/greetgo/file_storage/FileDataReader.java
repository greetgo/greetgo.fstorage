package kz.greetgo.file_storage;

import java.util.Date;

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
