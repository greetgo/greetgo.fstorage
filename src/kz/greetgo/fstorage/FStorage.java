package kz.greetgo.fstorage;

/**
 * Интенрфейс хранилища файлов
 * 
 * @author pompei
 */
public interface FStorage {
  /**
   * Создаёт и записывает новый файл
   * 
   * @param fileDot
   *          данные файла
   * @return идентификатор нового файла
   */
  long addNewFile(FileDot fileDot);
  
  /**
   * Получает файл
   * 
   * @param fileId
   *          ИД файла
   * @return дот файла или null, если указанного ИД не существует
   */
  FileDot getFile(long fileId);
}
