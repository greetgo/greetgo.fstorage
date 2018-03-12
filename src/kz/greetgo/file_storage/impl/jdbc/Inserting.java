package kz.greetgo.file_storage.impl.jdbc;

import kz.greetgo.file_storage.impl.FileStorageLogger;
import kz.greetgo.file_storage.impl.jdbc.model.Field;
import kz.greetgo.file_storage.impl.jdbc.model.FieldWithExpr;
import kz.greetgo.file_storage.impl.jdbc.model.FieldWithValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Inserting {


  private final Connection connection;
  private Function<String, String> sqlPreparation = (s) -> s;

  private static String view(String sql) {
    if (FileStorageLogger.isTraceEnabled()) {
      FileStorageLogger.traceSQL(sql);
    }
    return sql;
  }


  private Inserting(Connection connection) {
    this.connection = connection;
  }

  public static Inserting with(Connection connection) {
    return new Inserting(connection);
  }

  public Inserting sqlPreparation(Function<String, String> sqlPreparation) {
    this.sqlPreparation = sqlPreparation;
    return this;
  }

  public InsertInto into(String table) {
    return new InsertInto(table);
  }

  public class InsertInto {
    private String table;
    final List<Field> fields = new ArrayList<>();
    final List<String> appendToEnd = new ArrayList<>();

    public InsertInto(String table) {
      this.table = table;
    }

    public InsertInto field(String fieldName, Object fieldValue) {
      fields.add(new FieldWithValue(fieldName, fieldValue));
      return this;
    }

    public InsertInto fieldSkipNull(String fieldName, Object fieldValue) {
      if (fieldValue != null) fields.add(new FieldWithValue(fieldName, fieldValue));
      return this;
    }

    public InsertInto fieldTimestamp(String fieldName, Date fieldValue, boolean skipNull) {
      Timestamp v = fieldValue == null ? null : new Timestamp(fieldValue.getTime());
      if (!skipNull || v != null) fields.add(new FieldWithValue(fieldName, v));
      return this;
    }

    public InsertInto fieldExpr(String fieldName, String fieldExpression) {
      fields.add(new FieldWithExpr(fieldName, fieldExpression));
      return this;
    }

    public void go() throws SQLException {
      StringBuilder sql = new StringBuilder("insert into " + table + " (");
      sql.append(fields.stream().map(f -> f.name).collect(Collectors.joining(", ")));
      sql.append(") values (");
      sql.append(fields.stream().map(Field::place).collect(Collectors.joining(", ")));
      sql.append(")");
      appendToEnd.forEach(s -> sql.append(' ').append(s));

      try (PreparedStatement ps = connection.prepareStatement(view(sqlPreparation.apply(sql.toString())))) {

        int index = 1;
        //noinspection Duplicates
        for (Object value : fields.stream()
          .filter(f -> f instanceof FieldWithValue)
          .map(f -> ((FieldWithValue) f).value)
          .collect(Collectors.toList())
          ) {
          ps.setObject(index, value);
          if (FileStorageLogger.isTraceEnabled()) {
            FileStorageLogger.traceSqlParam(index, value);
          }
          index++;
        }


        ps.executeUpdate();
      }

    }

    public InsertInto appendToEnd(String sqlPart) {
      appendToEnd.add(sqlPart);
      return this;
    }
  }

}
