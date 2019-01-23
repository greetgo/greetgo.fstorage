package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.FileIdAlreadyExists;
import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.Ora00972_IdentifierIsTooLong;
import kz.greetgo.file_storage.impl.jdbc.Inserting;
import kz.greetgo.file_storage.impl.jdbc.Query;

import java.sql.Connection;
import java.sql.SQLException;

public class MonoDbOperationsOracle extends MonoDbOperationsPostgres {
  MonoDbOperationsOracle(FileStorageBuilderMonoDbImpl builder) {
    super(builder);

    if (builder.paramsTable.length() > 30) throw new Ora00972_IdentifierIsTooLong("builder.paramsTable = '"
      + builder.paramsTable + "' is too long. Specify at most 30 characters.");
    if (builder.dataTable.length() > 30) throw new Ora00972_IdentifierIsTooLong("builder.dataTable = '"
      + builder.dataTable + "' is too long. Specify at most 30 characters.");
    if (builder.paramsTableMimeType.length() > 30) throw new Ora00972_IdentifierIsTooLong(
      "builder.paramsTableMimeType = '" + builder.paramsTableMimeType + "' is too long. Specify at most 30 characters.");
  }

  @Override
  public String createNew(byte[] data, CreateNewParams params) throws DatabaseNotPrepared {
    try {
      return createNewEx(data, params);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String createNewEx(byte[] data, CreateNewParams params) throws SQLException {

    String sha1sum = sha1sum(data);

    try (Connection connection = builder.dataSource.getConnection()) {

      connection.setAutoCommit(false);
      try {

        try {


          StringBuilder sql = new StringBuilder();
//          structure.append("merge into __dataTable__ dest using ( select ? as id1, ? as data1 from dual ) src ");
//          structure.append(" on (dest.__dataTableId__ = src.id1 and dest.__dataTableData__ = src.data1)");
//          structure.append(" when not matched then insert (dest.__dataTableId__, dest.__dataTableData__)");
//          structure.append("                       values ( src.id1,              src.data1            )");

          sql.append("insert into __dataTable__ (__dataTableId__, __dataTableData__)");
          sql.append(" values (?, ?)");

          try (Query query = new Query(connection)) {
            query.sql.append(sql(sql.toString()));
            query.params.add(sha1sum);
            query.params.add(data);
            query.update();
          }

        } catch (Exception e) {
          String trimmedMessage = e.getMessage() == null ? "" : e.getMessage().trim();
          if (trimmedMessage.startsWith("ORA-00942:")) {
            throw new DatabaseNotPrepared();
          }
          boolean throwError = true;

          if (trimmedMessage.startsWith("ORA-00001:")) throwError = false;

          if (throwError) {
            if (e instanceof SQLException) throw (SQLException) e;
            //noinspection ConstantConditions
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
          }
        }

        String id = params.presetFileId != null ? params.presetFileId : builder.parent.idGenerator.get();

        try {
          Inserting.with(connection)
            .sqlPreparation(this::sql)
            .into("__paramsTable__")
            .field("__paramsTableId__", id)
            .field("__paramsTableName__", params.name)
            .field("__paramsTableMimeType__", params.mimeType)
            .field("__paramsTableDataId__", sha1sum)
            .fieldTimestamp("__paramsTableLastModifiedAt__", params.createdAt, true)
            .go()
          ;
        } catch (SQLException e) {
          String trimmedMessage = e.getMessage() == null ? "" : e.getMessage().trim();
          if (trimmedMessage.startsWith("ORA-00001:")) throw new FileIdAlreadyExists(id);
          throw e;
        }

        connection.commit();

        return id;

      } catch (SQLException | RuntimeException e) {
        connection.rollback();
        throw e;
      } finally {
        connection.setAutoCommit(true);
      }
    }

  }

  @Override
  public void prepareDatabase(DatabaseNotPrepared context) {
    try {
      prepareDatabaseEx(context);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void prepareDatabaseEx(@SuppressWarnings("unused") DatabaseNotPrepared context) throws SQLException {
    int idLen = builder.parent.fileIdLength;
    try (Connection connection = builder.dataSource.getConnection()) {

      try (Query query = new Query(connection)) {
        query.exec(sql("create table __dataTable__ (" +
          "   __dataTableId__   varchar2(40) not null primary key" +
          ",  __dataTableData__ blob" +
          ")"));

        query.exec(sql("create table __paramsTable__ (" +
          "   __paramsTableId__       varchar2(" + idLen + ") not null primary key" +
          ",  __paramsTableName__     varchar2(" + builder.paramsTableNameLength + ")" +
          ",  __paramsTableMimeType__ varchar2(" + builder.paramsTableMimeTypeLength + ")" +
          ",  __paramsTableDataId__   varchar2(40) not null references __dataTable__" +
          ",  __paramsTableLastModifiedAt__ timestamp default systimestamp not null" +
          ")"));
      }

    }
  }

  @Override
  protected String loadSha1SumSql() {
    return "select __paramsTableDataId__ from __paramsTable__ where __paramsTableId__ = ?";
  }

  protected void deleteEx(String fileId) throws SQLException, NoFileWithId {
    try (Connection connection = builder.dataSource.getConnection()) {

      String sha1sum = loadSha1sumByFileId(connection, fileId);

      doDelete(connection, "delete from __paramsTable__ where __paramsTableId__ = ?", fileId, fileId);
      doDelete(connection, "delete from __dataTable__ where __dataTableId__ = ?", sha1sum, fileId);

    }
  }
}
