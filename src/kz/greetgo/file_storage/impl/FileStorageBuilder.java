package kz.greetgo.file_storage.impl;

import javax.sql.DataSource;
import java.util.List;
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
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilder mandatoryName(boolean mandatoryName);

  /**
   * Указывает
   *
   * @param mandatoryMimeType необходимость обязательного определения MIME-типа файла
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilder mandatoryMimeType(boolean mandatoryMimeType);

  /**
   * Устанавливает
   *
   * @param validator проверщик корректности mimeType
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilder mimeTypeValidator(Function<String, Boolean> validator);

  /**
   * Заменяет генератор идентификаторов по-умолчанию на другой
   *
   * @param idGenerator другой генератор иднетификаторов
   * @param idLength    максимальная длинна в символах, генерируемых идентификаторов
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilder setIdGenerator(int idLength, Supplier<String> idGenerator);

  /**
   * Указывает хранение файлов в одной реляционной БД
   *
   * @param dataSource источник конектов к БД, чтобы создать хнанителя файлов в БД
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilderDb inDb(DataSource dataSource);

  /**
   * Указывает хранение файлов в нескольких реляционных БД
   *
   * @param dataSourceList список источников коннектов к релационным БД. Порядок в списке очень важет, так как данные
   *                       шардируются
   * @return ссылка на строителя для продолжения создания хранилища файлов
   */
  FileStorageBuilderMultiDb inMultiDb(List<DataSource> dataSourceList);
}
