package kz.greetgo.fstorage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import kz.greetgo.fstorage.FStorage;
import kz.greetgo.fstorage.FStorageConfig;
import kz.greetgo.fstorage.FileDot;
import kz.greetgo.util.ServerUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractFStorage implements FStorage {
  protected final DataSource dataSource;
  
  protected final FStorageConfig config;
  
  public int fieldFilenameLen = 300;
  
  public AbstractFStorage(DataSource dataSource, FStorageConfig config) {
    this.dataSource = dataSource;
    this.config = config;
  }
  
  private String table(long id) {
    int size = 0;
    {
      int a = config.tableCount;
      while (a > 0) {
        size++;
        a = a / 10;
      }
    }
    String nom = "" + (id % config.tableCount);
    while (nom.length() < size) {
      nom = "0" + nom;
    }
    return config.tableName + config.tableCount + '_' + nom;
  }
  
  @Override
  public long addNewFile(FileDot fileDot) {
    try {
      return addNewFileInner(fileDot);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private long addNewFileInner(FileDot fileDot) throws Exception {
    Connection con = dataSource.getConnection();
    try {
      return addNewFileCon(con, fileDot);
    } finally {
      con.close();
    }
  }
  
  protected abstract String nextIdSql(String sequenceName);
  
  private long nextId(Connection con) throws Exception {
    PreparedStatement ps = con.prepareStatement(nextIdSql(config.tableName + "_seq"));
    try {
      ResultSet rs = ps.executeQuery();
      try {
        rs.next();
        return rs.getLong(1);
      } finally {
        rs.close();
      }
    } finally {
      ps.close();
    }
  }
  
  @Override
  public FileDot getFile(long fileId) {
    try {
      return getFileInner(fileId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private void init(Connection con) throws Exception {
    if (!existsTable(con)) createTable(con);
  }
  
  private String sql(String name) {
    return ServerUtil.streamToStr(getClass().getResourceAsStream(name + ".sql"));
  }
  
  private boolean existsTable(Connection con) throws SQLException {
    PreparedStatement ps = con.prepareStatement(sql("exists_table"));
    try {
      ps.setString(1, table(0));
      ResultSet rs = ps.executeQuery();
      try {
        rs.next();
        return rs.getInt(1) > 0;
      } finally {
        rs.close();
      }
    } finally {
      ps.close();
    }
  }
  
  protected abstract String fieldTypeId();
  
  protected abstract String fieldTypeFilename();
  
  protected abstract String fieldTypeData();
  
  protected abstract String fieldTypeCreatedAt();
  
  protected abstract String fieldTypeSize();
  
  protected abstract String currentTimestampFunc();
  
  @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
  private void createTable(Connection con) throws Exception {
    {
      PreparedStatement ps = con.prepareStatement("create sequence " + config.tableName + "_seq");
      try {
        ps.executeUpdate();
      } finally {
        ps.close();
      }
    }
    for (int i = 0, C = config.tableCount; i < C; i++) {
      StringBuilder sql = new StringBuilder();
      sql.append("create table ").append(table(i)).append('(');
      sql.append("  id ").append(fieldTypeId()).append(" not null primary key,");
      if (config.hasCreatedAt) {
        sql.append("  createdAt ").append(fieldTypeCreatedAt())
            .append(" default " + currentTimestampFunc() + " not null,");
      }
      sql.append("  filename ").append(fieldTypeFilename()).append(',');
      sql.append("  data ").append(fieldTypeData());
      sql.append(')');
      
      try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
        ps.executeUpdate();
      }
    }
  }
  
  private long addNewFileCon(Connection con, FileDot fileDot) throws Exception {
    init(con);
    long ret = nextId(con);
    
    insertFileDot(con, ret, fileDot);
    
    return ret;
  }
  
  private int insertFileDot(Connection con, long id, FileDot fileDot) throws Exception {
    StringBuilder ins = new StringBuilder();
    ins.append("insert into ").append(table(id));
    ins.append(" (id,filename,data) values (?,?,?)");
    
    PreparedStatement ps = con.prepareStatement(ins.toString());
    try {
      ps.setLong(1, id);
      ps.setString(2, fileDot.filename);
      ps.setBytes(3, fileDot.data);
      return ps.executeUpdate();
    } finally {
      ps.close();
    }
  }
  
  private FileDot getFileInner(long id) throws Exception {
    Connection con = dataSource.getConnection();
    try {
      return getFileCon(con, id);
    } finally {
      con.close();
    }
  }
  
  private FileDot getFileCon(Connection con, long id) throws Exception {
    PreparedStatement ps = con.prepareStatement("select * from " + table(id) + " where id = ?");
    try {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      try {
        if (!rs.next()) return null;
        
        FileDot ret = new FileDot(rs.getString("filename"), rs.getBytes("data"));
        if (config.hasCreatedAt) {
          ret.createdAt = new Date(rs.getTimestamp("createdAt").getTime());
        }
        return ret;
      } finally {
        rs.close();
      }
    } finally {
      ps.close();
    }
  }
}
