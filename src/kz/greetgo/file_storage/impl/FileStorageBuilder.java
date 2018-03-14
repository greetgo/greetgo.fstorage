package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

import javax.sql.DataSource;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Строитель хранилища файлов
 */
public interface FileStorageBuilder {

  /**
   * Создаёт
   *
   * @return новый строитель хранилища файлов
   */
  static FileStorageBuilder newBuilder() {
    return new FileStorageBuilderImpl();
  }

  /**
   * Указывает
   *
   * @param mandatoryName необходимость обязательного определения имени файла
   * @return ссылка на этого строителя для возможности точечной нотации
   */
  FileStorageBuilder mandatoryName(boolean mandatoryName);

  /**
   * Указывает
   *
   * @param mandatoryMimeType необходимость обязательного определения MIME-типа файла
   * @return ссылка на этого строителя для возможности точечной нотации
   */
  FileStorageBuilder mandatoryMimeType(boolean mandatoryMimeType);

  /**
   * Устанавливает
   *
   * @param validator проверщик корректности mimeType
   * @return ссылка на этого строителя для возможности точечной нотации
   */
  FileStorageBuilder mimeTypeValidator(Function<String, Boolean> validator);

  /**
   * Заменяет генератор идентификаторов по-умолчанию на другой
   *
   * @param idGenerator другой генератор иднетификаторов
   * @param idLength    максимальная длинна в символах, генерируемых идентификаторов
   * @return ссылка на этого строителя для возможности точечной нотации
   */
  FileStorageBuilder idGenerator(int idLength, Supplier<String> idGenerator);

  /**
   * Указывает
   *
   * @param dataSource источник донектов к БД, чтобы создать хнанителя файлов в БД
   * @return ссылка на этого строителя для возможности точечной нотации
   */
  FileStorageBuilderDb inDb(DataSource dataSource);
}
