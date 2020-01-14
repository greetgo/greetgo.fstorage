package kz.greetgo.file_storage.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import kz.greetgo.file_storage.FileStorage;
import kz.greetgo.file_storage.impl.util.RND;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ExampleMongoGridFs {
  public static void main(String[] args) {

    MongoClient mongoClient = new MongoClient();
    MongoDatabase db = mongoClient.getDatabase("example_grid_fs");

    FileStorage fileStorage = FileStorageBuilder
      .newBuilder()
//      .setIdGenerator(35, () -> RND.str(10))
      .inMongoGridFs(db)
//      .useObjectId(false)
      .build();

    String content1 = RND.str(100);

    String fileId = fileStorage.storing().data(content1.getBytes(UTF_8)).store();

    System.out.println("fileId = " + fileId);

    byte[] bytes = fileStorage.read(fileId).dataAsArray();

    String content2 = new String(bytes, UTF_8);

    System.out.println(content1);
    System.out.println(content2);

  }

}
