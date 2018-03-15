package kz.greetgo.file_storage.impl;

public class MultiDbOperationsOracle extends MultiDbOperationsPostgres {
  @Override
  protected String strType(int len) {
    return "varchar2(" + len + ")";
  }

  @Override
  protected String timestampType() {
    return "timestamp";
  }

  @Override
  protected String blobType() {
    return "blob";
  }

  @Override
  protected String currentTimestampFunc() {
    return "systimestamp";
  }
}
