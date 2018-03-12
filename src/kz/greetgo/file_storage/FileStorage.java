package kz.greetgo.file_storage;

import kz.greetgo.file_storage.errors.NoFileWithId;

/**
 * Хранилище файлов
 */
public interface FileStorage {

  /**
   * Начинает сохранение нового файла
   *
   * @return сохранитель нового файла
   */
  FileStoringOperation storing();

  /**
   * Чтение файла по идентификатору
   *
   * @param fileId идентификатор читаемого файла
   * @return читатель данных файла
   * @throws NoFileWithId выбрасывается, если не найден файл с указанным идентификатором
   */
  FileDataReader read(String fileId) throws NoFileWithId;

  /**
   * Чтение файла по идентификатору
   *
   * @param fileId идентификатор читаемого файла
   * @return читатель данных файла
   */
  FileDataReader readOrNull(String fileId);
}
