package kz.greetgo.file_storage.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
  public static byte[] readAll(InputStream inputStream) {
    try {
      return readAllEx(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] readAllEx(InputStream inputStream) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[4 * 1024];

    while (true) {
      int count = inputStream.read(buffer);
      if (count < 0) return out.toByteArray();
      out.write(buffer, 0, count);
    }
  }
}
