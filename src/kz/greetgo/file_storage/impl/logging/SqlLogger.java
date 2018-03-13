package kz.greetgo.file_storage.impl.logging;

import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerErrorEvent;
import kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEvent;

public interface SqlLogger {
  boolean isTraceEnabled();

  void trace(FileStorageLoggerEvent event);

  void error(FileStorageLoggerErrorEvent event);
}
