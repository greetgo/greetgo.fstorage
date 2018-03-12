package kz.greetgo.file_storage;

import java.util.Date;

/**
 * Читатель данных файла
 */
public interface FileDataReader {
  /**
   * Читает
   *
   * @return имя файла
   */
  String name();

  /**
   * Читает
   *
   * @return содержимое файла, как массив байт
   */
  byte[] dataAsArray();

  /**
   * Читает
   *
   * @return дату и время последней модификации файла
   */
  Date lastModifiedAt();

  /**
   * Читает
   *
   * @return MIME-тип содержимого файла
   */
  String mimeType();

  /**
   * Читает
   *
   * @return идентификатор файла
   */
  String id();
}
