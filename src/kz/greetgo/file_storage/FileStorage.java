package kz.greetgo.file_storage;

import kz.greetgo.file_storage.errors.NoFileWithId;

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
