package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.FileStorage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface FileStorageCreator {
  FileStorage create();

  static FileStorage selectDb(DataSource dataSource,
                              FileStorageCreator forPostgres,
                              FileStorageCreator forOracle) {

    try (Connection connection = dataSource.getConnection()) {
      String db = connection.getMetaData().getDatabaseProductName().toLowerCase();
      if ("postgresql".equals(db)) return forPostgres.create();
      if ("oracle".equals(db)) return forOracle.create();
      throw new RuntimeException("Cannot create for DB type : " + db);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
