package kz.greetgo.file_storage.impl;

import org.bson.types.Binary;

import java.util.Date;

public class MongoUtil {
  public static String toStr(Object objectValue) {
    if (objectValue == null) {
      return null;
    }
    if (objectValue instanceof String) {
      return (String) objectValue;
    }
    throw new IllegalArgumentException("Cannot convert to string the value of "
      + objectValue.getClass() + " = " + objectValue);
  }

  public static Date toDate(Object objectValue) {
    if (objectValue == null) {
      return null;
    }
    if (objectValue instanceof Date) {
      return (Date) objectValue;
    }
    throw new IllegalArgumentException("Cannot convert to Date the value of "
      + objectValue.getClass() + " = " + objectValue);
  }

  public static byte[] toByteArray(Object objectValue) {
    if (objectValue == null) {
      return null;
    }

    if (objectValue instanceof String) {
      String base64 = (String) objectValue;
      return Base64Util.base64ToBytes(base64);
    }

    if (objectValue instanceof Binary) {
      Binary bin = (Binary) objectValue;
      return bin.getData();
    }

    throw new IllegalArgumentException("Cannot convert to byte[] the value of "
      + objectValue.getClass() + " = " + objectValue);
  }
}

