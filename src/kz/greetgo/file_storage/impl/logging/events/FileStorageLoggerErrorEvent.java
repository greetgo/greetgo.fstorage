package kz.greetgo.file_storage.impl.logging.events;

public interface FileStorageLoggerErrorEvent extends FileStorageLoggerEvent {

  Exception error();

}
