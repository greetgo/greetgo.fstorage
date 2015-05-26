package kz.greetgo.fstorage;

import java.util.Date;

/**
 * 
 * Хранитель файла - содержит контент файла с именем
 * 
 * @author pompei
 */
public class FileDot {
  /**
   * Имя файла
   */
  public String filename;
  
  /**
   * Содержимое файла
   */
  public byte[] data;
  
  /**
   * Дата-время сохранения файла
   */
  public Date createdAt;
  
  /**
   * Размер файла (может не использоваться)
   */
  public Integer size;
  
  public FileDot() {}
  
  public FileDot(String filename, byte[] data) {
    this.filename = filename;
    this.data = data;
  }
}
