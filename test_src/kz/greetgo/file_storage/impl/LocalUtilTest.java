package kz.greetgo.file_storage.impl;

import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocalUtilTest {
  @Test
  public void extractExtension_OK() throws Exception {
    String extension = LocalUtil.extractExtension("hello.txt");
    assertThat(extension).isEqualTo("txt");
  }

  @Test
  public void extractExtension_no() throws Exception {
    String extension = LocalUtil.extractExtension("hello");
    assertThat(extension).isNull();
  }

  @Test
  public void extractExtension_lowerCase() throws Exception {
    String extension = LocalUtil.extractExtension("   hello.different_CASES    ");
    assertThat(extension).isEqualTo("different_cases");
  }

  @Test
  public void extractExtension_manyComma() throws Exception {
    String extension = LocalUtil.extractExtension("   file.name.wow.this_IS_extension    ");
    assertThat(extension).isEqualTo("this_is_extension");
  }

  @Test
  public void extractExtension_lastComma() throws Exception {
    String extension = LocalUtil.extractExtension("   file.name.wow.   ");
    assertThat(extension).isEqualTo("");
  }
}
