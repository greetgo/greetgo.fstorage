package kz.greetgo.file_storage.impl.logging;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class FileStorageLoggerTest {

  @Test
  public void switchToLog4j() throws Exception {
    FileStorageLogger.switchToLog4j("TEST_SQL");

    List<Object> params = new ArrayList<>();
    params.add("asd");
    params.add("asd2");
    FileStorageLogger.traceSelect("select * from dual", params, 10, 20, 30, 40);
  }
}