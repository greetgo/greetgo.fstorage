package kz.greetgo.file_storage.errors;

import kz.greetgo.file_storage.impl.TablePosition;

public class TableIsAbsent extends RuntimeException {
  public final TablePosition tablePosition;

//  @Override
//  public synchronized Throwable fillInStackTrace() {
//    return this;
//  }

  public TableIsAbsent(TablePosition tablePosition) {
    this.tablePosition = tablePosition;
  }
}
