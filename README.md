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
  public static FileStorage getFileStorage() {
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

## Multiple relational database usage

```java
public class FileStorageFactory {
  public static FileStorage getFileStorage() {
    javax.sql.DataSource dataSource1 = getCoolDataSource(1);
    javax.sql.DataSource dataSource2 = getCoolDataSource(2);
    javax.sql.DataSource dataSource3 = getCoolDataSource(3);
    // any number of data source
    
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .mandatoryMimeType(true)//make defining mime type mandatory
      .mandatoryName(true)//make defining file name mandatory
      .inMultiDb(Arrays.asList(dataSource1, dataSource2, dataSource3))//define place to store files
      .build();
    
    return fileStorage;
  }
}
```

## MongoDB usage

```java
public class FileStorageFactory {
  public static FileStorage getFileStorage() {
    MongoCollection<Document> collection = getCollectionForFileStorage();
    
    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
      .mandatoryMimeType(true)//make defining mime type mandatory
      .mandatoryName(true)//make defining file name mandatory
      .inMongodb(collection)//define place to store files
      .build();
    
    return fileStorage;
  }
}
```

FileStorageBuilder can configure many parameters: table name, field names, etc.

## Examples

```java
import kz.greetgo.file_storage.FileStorage;

public class Examples {
  public static void main(String[] args) {
    javax.sql.DataSource dataSource = getCoolDataSource();
    
    FileStorage fileStorage = getFileStorage();
    
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
