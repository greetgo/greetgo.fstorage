package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.NoFileWithId;
import kz.greetgo.file_storage.errors.TableIsAbsent;
import kz.greetgo.file_storage.impl.jdbc.Query;
import kz.greetgo.file_storage.impl.jdbc.insert.Insert;
import kz.greetgo.file_storage.impl.jdbc.structure.Field;
import kz.greetgo.file_storage.impl.jdbc.structure.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class MultiDbOperationsPostgres implements MultiDbOperations {
  @Override
  public void createTableQuiet(DataSource dataSource, Table table) {
    StringBuilder sql = new StringBuilder();
    sql.append("create table ").append(table.name).append(" (\n");

    {
      sql.append(table.fieldList
        .stream()
        .map(this::createField)
        .collect(Collectors.joining(",\n  ", "  ", ",\n")));

      sql.append(table.fieldList
        .stream()
        .filter(f -> f.primaryKey)
        .map(f -> f.name)
        .collect(Collectors.joining(", ", "  primary key(", ")\n")));
    }

    sql.append(")");

    try (Connection connection = dataSource.getConnection(); Query query = new Query(connection)) {

      query.exec(sql);

    } catch (SQLException e) {
      if ("23505".equals(e.getSQLState())) return;
      if ("42P07".equals(e.getSQLState())) return;
      if ("42710".equals(e.getSQLState())) return;
      if (e.getMessage().startsWith("ORA-00955:")) return;
      throw new RuntimeException("e.getSQLState() = " + e.getSQLState() + " :: " + e.getMessage(), e);
    }
  }

  private String createField(Field field) {
    return field.name + " " + fieldType(field);
  }

  private String fieldType(Field field) {
    switch (field.type) {
      case STR:
        return strType(field.valueLen) + ' ' + notNull(field);
      case TIMESTAMP:
        return timestampType() + ' ' + defaultTimestamp(field) + ' ' + notNull(field);
      case BLOB:
        return blobType() + ' ' + notNull(field);
      default:
        throw new IllegalArgumentException("field.type = " + field.type);
    }
  }

  protected String strType(int len) {
    return "varchar(" + len + ")";
  }

  protected String timestampType() {
    return "timestamp";
  }

  protected String blobType() {
    return "byteA";
  }

  protected String currentTimestampFunc() {
    return "current_timestamp";
  }

  private String defaultTimestamp(Field field) {
    return field.defaultCurrentTimestamp ? "default " + currentTimestampFunc() : "";
  }

  private String notNull(Field field) {
    return field.notNull ? "not null" : "";
  }

  @Override
  public void insert(DataSource dataSource, Insert insert, TablePosition tablePosition) throws TableIsAbsent {
    try (Connection connection = dataSource.getConnection(); Query query = new Query(connection)) {

      query.sql.append("insert into ").append(insert.tableName).append(" (");

      query.sql.append(insert.fieldList
        .stream()
        .map(f -> f.name)
        .collect(Collectors.joining(", ")));

      query.sql.append(") values (");

      query.sql.append(insert.fieldList
        .stream()
        .map(f -> "?")
        .collect(Collectors.joining(", ")));

      query.sql.append(")");

      query.params = insert.fieldList
        .stream()
        .map(f -> f.value)
        .collect(Collectors.toList());

      query.update();

    } catch (SQLException e) {
      if ("42P01".equals(e.getSQLState())) throw new TableIsAbsent(tablePosition);
      if (e.getMessage().startsWith("ORA-00942:")) throw new TableIsAbsent(tablePosition);
      throw new RuntimeException("e.getSQLState() = " + e.getSQLState() + " :: " + e.getMessage(), e);
    }
  }

  @Override
  public byte[] loadData(DataSource dataSource, String tableName,
                         String idFieldName, String idValue,
                         String gettingFieldName) {

    try (Connection connection = dataSource.getConnection(); Query query = new Query(connection)) {

      query.sql.append("select ").append(gettingFieldName);
      query.sql.append(" from ").append(tableName);
      query.sql.append(" where ").append(idFieldName).append(" = ?");
      query.params.add(idValue);

      query.go();

      if (!query.rs().next()) throw new NoFileWithId(idValue);

      return query.rs().getBytes(1);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public FileParams loadFileParams(DataSource dataSource, String tableName, String fileId, TableFieldNames names) {
    try (Connection connection = dataSource.getConnection(); Query query = new Query(connection)) {

      query.sql.append("select ").append(names.join());
      query.sql.append(" from ").append(tableName);
      query.sql.append(" where ").append(names.id).append(" = ?");
      query.params.add(fileId);

      query.go();

      if (!query.rs().next()) return null;

      {
        FileParams ret = new FileParams();
        ret.id = query.rs().getString(names.id);
        ret.name = query.rs().getString(names.name);
        ret.mimeType = query.rs().getString(names.mimeType);
        ret.lastModifiedBy = query.rs().getTimestamp(names.createdAt);
        return ret;
      }

    } catch (SQLException e) {
      if ("42P01".equals(e.getSQLState())) return null;
      throw new RuntimeException("e.getSQLState() = " + e.getSQLState() + " :: " + e.getMessage(), e);
    }
  }
}
