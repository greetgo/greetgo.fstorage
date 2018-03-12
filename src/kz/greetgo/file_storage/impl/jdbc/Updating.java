package kz.greetgo.file_storage.impl.jdbc;

import kz.greetgo.file_storage.impl.FileStorageLogger;
import kz.greetgo.file_storage.impl.jdbc.model.Field;
import kz.greetgo.file_storage.impl.jdbc.model.FieldWithExpr;
import kz.greetgo.file_storage.impl.jdbc.model.FieldWithValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Updating {

  private static String view(String sql) {
    if (FileStorageLogger.isTraceEnabled()) {
      FileStorageLogger.traceSQL(sql);
    }
    return sql;
  }

  private final Connection connection;
  private final String table;

  private Updating(Connection connection, String table) {
    this.connection = connection;
    this.table = table;
  }

  public static Updating create(Connection jdbc, String table) {
    return new Updating(jdbc, table);
  }

  final List<Field> setFields = new ArrayList<>();

  public Updating field(String fieldName, Object fieldValue) {
    setFields.add(new FieldWithValue(fieldName, fieldValue));
    return this;
  }

  public Updating fieldExpr(String fieldName, String fieldExpression) {
    setFields.add(new FieldWithExpr(fieldName, fieldExpression));
    return this;
  }

  final List<Field> whereFields = new ArrayList<>();

  public Updating where(String fieldName, Object fieldValue) {
    whereFields.add(new FieldWithValue(fieldName, fieldValue));
    return this;
  }

  public Updating whereExpr(String fieldName, String fieldExpression) {
    whereFields.add(new FieldWithExpr(fieldName, fieldExpression));
    return this;
  }

  public Updating whereExpr(String expression) {
    return whereExpr(null, expression);
  }

  public int execute() {
    //noinspection StringBufferReplaceableByString
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(table).append(" set ");
    sql.append(setFields.stream().map(f -> f.name + " = " + f.place()).collect(Collectors.joining(", ")));
    sql.append(" where ");
    sql.append(whereFields.stream().map(Field::expression).collect(Collectors.joining(" AND ")));

    try (PreparedStatement ps = connection.prepareStatement(view(sql.toString()))) {

      setFields.addAll(whereFields);
      int index = 1;
      //noinspection Duplicates
      for (Object value : setFields.stream()
        .filter(f -> f instanceof FieldWithValue)
        .map(f -> ((FieldWithValue) f).value)
        .collect(Collectors.toList())) {
        ps.setObject(index, value);
        if (FileStorageLogger.isTraceEnabled()) {
          FileStorageLogger.traceSqlParam(index, value);
        }
        index++;
      }

      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }


  }

}
