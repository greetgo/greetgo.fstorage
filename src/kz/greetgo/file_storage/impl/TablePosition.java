package kz.greetgo.file_storage.impl;

public class TablePosition {
  public final int dbIndex, tableIndex;

  public TablePosition(int dbIndex, int tableIndex) {
    this.dbIndex = dbIndex;
    this.tableIndex = tableIndex;
  }
}
