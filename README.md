# File Storage in DB

Usage:

```java
import kz.greetgo.file_storage.FileStorage;

public class Examples {
  public static void main(String[] args) {
    javax.sql.DataSource dataSource = getCoolDataSource();
    
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .configureFrom(MimeTypeBaseConfigurator.get())//defines using mime types, you can redefine you want
      .mandatoryMimeType(true)//make defining mime type mandatory
      .mandatoryName(true)//make defining file name mandatory
      .inDb(dataSource)//define place to store files
      .build();
    
    //At now you can add file
    
    String fileId = fileStorage.storing()
          .name("hello.txt")//MimeType calculates by extension
          .data("File content bla bla bla".getBytes(StandardCharsets.UTF_8))
          .store();
    
    //And now you can read stored file
    FileDataReader reader = fileStorage.read(fileId);
    System.out.println("name      = " + reader.name());
    System.out.println("Mime type = " + reader.mimeType());
    System.out.println("createdAt = " + reader.createdAt());
    byte[] dataAsArray = reader.dataAsArray();
    String data = new String(dataAsArray, UTF_8);
    System.out.println(data);
  }
}
```
