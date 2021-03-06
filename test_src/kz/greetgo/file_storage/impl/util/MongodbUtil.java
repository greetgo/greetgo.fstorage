package kz.greetgo.file_storage.impl.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Filters.eq;

public class MongodbUtil {

  public static MongoClient createMongoClient() {
    return new MongoClient();
  }

  public static MongoDatabase createDatabase(String collectionName) {
    MongoClient mongoClient = createMongoClient();
    return mongoClient.getDatabase(System.getProperty("user.name") + "_" + collectionName);
  }

  public static MongoCollection<Document> connectGetCollection(String collectionName) {
    MongoDatabase database = createDatabase(collectionName);
    return database.getCollection("fileStorage");
  }

  private static final AtomicReference<Boolean> cachedHasMongodbResult = new AtomicReference<>(null);

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean hasMongodb() {
    {
      Boolean result = cachedHasMongodbResult.get();
      if (result != null) {
        return result;
      }
    }

    final AtomicReference<Boolean> detectedResult = new AtomicReference<>(null);

    Thread detector = new Thread(() -> {
      try {
        connectGetCollection("probe_collection").find(eq("asd", 1)).first();
        detectedResult.set(true);
      } catch (MongoSocketOpenException | MongoTimeoutException ignore) {
        detectedResult.set(false);
      }
    });

    detector.start();

    try {
      detector.join(1000);
    } catch (InterruptedException ignore) {}

    Boolean detectedRes = detectedResult.get();

    final boolean ret;

    if (detectedRes == null) {
      ret = false;
      detector.interrupt();
    } else {
      ret = detectedRes;
    }

    cachedHasMongodbResult.set(ret);

    return ret;
  }
}
