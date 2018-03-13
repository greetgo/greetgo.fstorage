package kz.greetgo.file_storage.impl.logging.events;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileStorageLoggerEventsUtilTest {

  @DataProvider
  public Object[][] doubleToStr_DP() {
    return new Object[][]{
      {123.34, "123.34"},
      {123.345432665, "123.3454327"},
      {1233454.32665, "1233454.32665"},
      {0.0000234232665, "0.0000234"},
      {0.0000000232665, "2.32665e-8"},
      {0.000000000654677, "6.54677e-10"},
    };
  }

  @Test(dataProvider = "doubleToStr_DP")
  public void doubleToStr(double value, String expected) throws Exception {
    String s = FileStorageLoggerEventsUtil.doubleToStr(value);
    assertThat(s).isEqualTo(expected);
  }
}