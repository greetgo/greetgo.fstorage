package kz.greetgo.file_storage;

import java.io.InputStream;
import java.util.Date;

/**
 * Операция по сохранению файла
 */
public interface FileStoringOperation {
  /**
   * Указывает
   *
   * @param name имя файла
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation name(String name);

  /**
   * Указывает
   *
   * @param lastModifiedAt дату и время последней модификации
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation lastModifiedAt(Date lastModifiedAt);

  /**
   * Указывает
   * @param mimeType тип данных MIME
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation mimeType(String mimeType);

  /**
   * Устанавливает контент файла через массив байтов
   *
   * @param data контент файла
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation data(byte[] data);

  /**
   * Устанавливает контент файла через input stream
   *
   * @param inputStream данные для контента файла
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation data(InputStream inputStream);

  /**
   * Запуск производимой операции
   *
   * @return идентификатор сохранённого файла
   */
  String store();

  /**
   * Устанавливает идентификатор файла. Такого идентификатора в БД не должно быть. Если он есть, то сгенерируется ошибка
   *
   * @param presetFileId идентификатор файла
   * @return ссылка на проводимую операцию
   */
  FileStoringOperation presetId(String presetFileId);
}
