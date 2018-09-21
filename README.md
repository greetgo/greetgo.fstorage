# File Storage in DB

You can use the following databases:

 - Single relational database
   - PostgreSQL
   - Oracle
 - Multiple relational database
   - PostgreSQL
   - Oracle
 - MongoDB

See source of [FileStorage](https://github.com/greetgo/greetgo.fstorage/blob/master/src/kz/greetgo/file_storage/FileStorage.java)

## Single relational database usage

```java
public class FileStorageFactory {
  public static FileStorage getFileStorage(String[] args) {
    javax.sql.DataSource dataSource = getCoolDataSource();
    
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .mandatoryMimeType(true)//make defining mime type mandatory
      .mandatoryName(true)//make defining file name mandatory
      .inDb(dataSource)//define place to store files
      .build();
    
    return fileStorage;
  }
}
```

## Examples

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

You can also store files in different databases, if files too many, like following:

```java
public class Examples {
  public static void main(String[] args) {
    DataSource dataSource1 = getCoolDataSource1();
    DataSource dataSource2 = getCoolDataSource2();
    DataSource dataSource3 = getCoolDataSource3();

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .inMultiDb(Arrays.asList(dataSource1, dataSource2, dataSource3))
      .build();
    
    //At now you can work with fileStorage...
  }
}
```

FileStorageBuilder can configure many parameters: table name, field names, etc.

