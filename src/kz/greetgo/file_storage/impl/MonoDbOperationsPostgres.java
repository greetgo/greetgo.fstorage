package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.FileIdAlreadyExists;
import kz.greetgo.file_storage.impl.jdbc.Inserting;
import kz.greetgo.file_storage.impl.jdbc.Query;

import java.sql.Connection;
import java.sql.SQLException;

public class MonoDbOperationsPostgres extends AbstractMonoDbOperations {
  MonoDbOperationsPostgres(FileStorageBuilderMonoDbImpl builder) {
    super(builder);
  }

  @Override
  public String createNew(byte[] data, CreateNewParams params) throws DatabaseNotPrepared {
    try {
      return createNewEx(data, params);
    } catch (SQLException e) {
      throw prepareException(e);
    }
  }

  private String createNewEx(byte[] data, CreateNewParams params) throws SQLException {

    String sha1sum = sha1sum(data);

    try (Connection connection = builder.dataSource.getConnection()) {

      connection.setAutoCommit(false);
      try {

        Inserting.with(connection)
          .sqlPreparation(this::sql)
          .into("__dataTable__")
          .field("__dataTableId__", sha1sum)
          .field("__dataTableData__", data)
          .appendToEnd("ON CONFLICT DO NOTHING")
          .go()
        ;

        String id = params.presetFileId != null ? params.presetFileId : builder.parent.idGenerator.get();

        try {
          Inserting.with(connection)
            .sqlPreparation(this::sql)
            .into("__paramsTable__")
            .field("__paramsTableId__", id)
            .field("__paramsTableName__", params.name)
            .field("__paramsTableMimeType__", params.mimeType)
            .field("__paramsTableDataId__", sha1sum)
            .fieldTimestamp("__paramsTableLastModifiedAt__", params.lastModifiedAt, true)
            .go()
          ;
        } catch (SQLException e) {
          if ("23505".equals(e.getSQLState())) throw new FileIdAlreadyExists(id);
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
      throw prepareException(e);
    }
  }

  private static RuntimeException prepareException(SQLException e) {
    {
      String sqlState = e.getSQLState();
      if ("42P01".equals(sqlState)) throw new DatabaseNotPrepared();
    }
    return new RuntimeException(e);
  }

  private void prepareDatabaseEx(@SuppressWarnings("unused") DatabaseNotPrepared context) throws SQLException {
    int idLen = builder.parent.fileIdLength;
    try (Connection connection = builder.dataSource.getConnection()) {

      try (Query query = new Query(connection)) {
        query.exec(sql("create table __dataTable__ (" +
          "   __dataTableId__   varchar(40) not null primary key" +
          ",  __dataTableData__ bytea" +
          ")"));

        query.exec(sql("create table __paramsTable__ (" +
          "   __paramsTableId__      varchar(" + idLen + ") not null primary key" +
          ",  __paramsTableName__    varchar(" + builder.paramsTableNameLength + ")" +
          ",  __paramsTableMimeType__    varchar(" + builder.paramsTableMimeTypeLength + ")" +
          ",  __paramsTableDataId__  varchar(40) not null references __dataTable__" +
          ",  __paramsTableLastModifiedAt__  timestamp not null default current_timestamp" +
          ")"));
      }

    }
  }


  @Override
  public FileParams readParams(String fileId) {
    try {
      return readParamsEx(fileId);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected FileParams readParamsEx(String fileId) throws SQLException {
    try (Connection connection = builder.dataSource.getConnection()) {

      try (Query query = new Query(connection)) {
        query.sql.append(sql("select * from __paramsTable__ where __paramsTableId__ = ?"));
        query.params.add(fileId);

        query.go();

        if (!query.rs().next()) return null;

        FileParams ret = new FileParams();

        ret.id = query.rs().getString(builder.getParamsTableId());
        ret.sha1sum = query.rs().getString(builder.getParamsTableDataId());
        ret.name = query.rs().getString(builder.getParamsTableName());
        ret.mimeType = query.rs().getString(builder.getParamsTableMimeType());
        ret.createdAt = query.rs().getTimestamp(builder.getParamsTableLastModifiedAt());

        return ret;
      }

    }

  }

  @Override
  public byte[] getDataAsArray(String sha1sum) {
    try {
      return getDataAsArrayEx(sha1sum);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected byte[] getDataAsArrayEx(String sha1sum) throws SQLException {
    try (Connection connection = builder.dataSource.getConnection()) {

      try (Query query = new Query(connection)) {
        query.sql.append(sql("select __dataTableData__ from __dataTable__ where __dataTableId__ = ?"));
        query.params.add(sha1sum);

        query.go();

        if (!query.rs().next()) throw new RuntimeException("No data for sha1sum = " + sha1sum);
        return query.rs().getBytes(1);
      }

    }
  }
}
