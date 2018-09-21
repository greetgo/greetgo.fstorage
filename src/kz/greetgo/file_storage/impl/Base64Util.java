package kz.greetgo.file_storage.impl;

import java.util.Base64;

public class Base64Util {

  public static byte[] base64ToBytes(String base64) {
    if (base64 == null) {
      return null;
    }
    try {
      final byte[] ret = Base64.getDecoder().decode(base64);
      if (ret == null) {
        return null;
      }
      if (ret.length == 0) {
        return null;
      }
      return ret;
    } catch (Exception e) {
      return null;
    }
  }

  public static String bytesToBase64(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    return Base64.getEncoder().encodeToString(bytes);
  }

}
