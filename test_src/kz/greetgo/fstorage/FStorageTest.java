package kz.greetgo.fstorage;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.fest.assertions.api.Assertions.assertThat;

public class FStorageTest extends MyTestBase {

  private static class LocalFileDot {
    long id;
    String filename;
    byte[] data;
  }

  private static final String ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String abc = "abcdefghijklmnopqrstuvwxyz";
  private static final String DEG = "0123456789";
  private static final String ALL_ENG = ABC + abc + DEG;

  @SuppressWarnings("SameParameterValue")
  private static String rndStr(int len, Random rnd) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      sb.append(ALL_ENG.charAt(rnd.nextInt(ALL_ENG.length())));
    }
    return sb.toString();
  }

  private static byte rndByte(Random rnd) {
    return (byte) rnd.nextInt(256);
  }

  @DataProvider
  public Object[][] addNewFile_getFile_DP() throws Exception {

    List<Object[]> ret = new ArrayList<>();

    for (Object[] mas : dataProvider()) {
      Object dataSource = mas[0];

      ret.add(new Object[]{dataSource, false});
      ret.add(new Object[]{dataSource, true});

    }

    return ret.toArray(new Object[ret.size()][]);
  }

  @Test(dataProvider = "addNewFile_getFile_DP")
  public void addNewFile_getFile(DataSource dataSource, boolean hasCreatedAt) throws Exception {
    Random rnd = new Random();

    final List<LocalFileDot> lfdList = new ArrayList<>();

    for (int i = 0; i < 110; i++) {
      LocalFileDot x = new LocalFileDot();
      x.filename = rndStr(10, rnd);
      x.data = new byte[10 + rnd.nextInt(10)];
      for (int j = 0, C = x.data.length; j < C; j++) {
        x.data[j] = rndByte(rnd);
      }
      lfdList.add(x);
    }

    FStorageFactory f = new FStorageFactory();
    f.setDataSource(dataSource);
    FStorageConfig config = new FStorageConfig("test_file_table", 10, hasCreatedAt);
    f.setConfig(config);

    dropAllTables(dataSource.getConnection(), "test_file_table", 10);

    FStorage fs = f.create();

    for (LocalFileDot lfd : lfdList) {
      lfd.id = fs.addNewFile(new FileDot(lfd.filename, lfd.data));
    }

    for (LocalFileDot lfd : lfdList) {
      FileDot fd = fs.getFile(lfd.id);
      assertThat(fd).isNotNull();
      assertThat(fd.filename).isEqualTo(lfd.filename);
      assertThat(fd.data).isEqualTo(lfd.data);
      if (hasCreatedAt) {
        assertThat(fd.createdAt).isNotNull();
      }
    }
  }

  private void dropAllTables(Connection con, String tableSuffix, int tableCount)
    throws SQLException {

    int size = 0;
    {
      int a = tableCount;
      while (a > 0) {
        size++;
        a = a / 10;
      }
    }

    for (int i = 0; i < tableCount; i++) {

      String nom = "" + i;
      while (nom.length() < size) {
        nom = "0" + nom;
      }

      String tn = tableSuffix + tableCount + '_' + nom;

      queryForce(con, "drop table " + tn);
    }

    queryForce(con, "drop sequence " + tableSuffix + "_seq");

    con.close();
  }
}
