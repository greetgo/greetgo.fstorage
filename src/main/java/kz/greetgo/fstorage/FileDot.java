package kz.greetgo.fstorage;

import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
  
  public FileDot() {}
  
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public FileDot(String filename, byte[] data) {
    this.filename = filename;
    this.data = data;
  }
}
