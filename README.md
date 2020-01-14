# File Storage in DB

Using this library you can store files in the databases listed below: store files under fileId
and get files by fileId.

FileId generates automatically and can be used in browser address bar (F6), or you can specify fileId by yourself.

Also you can store/get file names, fileMimeTypes and createdAt time.

Do it using [kz.greetgo.file_storage.FileStorage](https://github.com/greetgo/greetgo.fstorage/blob/master/src/kz/greetgo/file_storage/FileStorage.java)

There is `kz.greetgo.file_storage.impl.FileStorageBuilder.newBuilder()` to create implementations of FileStorage

You can use the following databases:

 - Single relational database
   - PostgreSQL
   - Oracle
 - Multiple relational database
   - PostgreSQL
   - Oracle
 - MongoDB

## Installing

In Maven

```xml
<dependency>
    <groupId>kz.greetgo</groupId>
    <artifactId>greetgo.fstorage</artifactId>
    <version>2.1.1</version>
</dependency>
```

In gradle

```groovy
compile "kz.greetgo:greetgo.fstorage:2.1.1"
```

## Single relational database usage

```java
public class FileStorageFactory {
  public static FileStorage getFileStorage() {
    javax.sql.DataSource dataSource = getCoolDataSource();
    
    kz.greetgo.file_storage.FileStorage fileStorage = FileStorageBuilder
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
    
    kz.greetgo.file_storage.FileStorage fileStorage = FileStorageBuilder
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
    
    kz.greetgo.file_storage.FileStorage fileStorage = FileStorageBuilder
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
