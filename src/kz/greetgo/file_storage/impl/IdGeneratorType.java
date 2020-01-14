package kz.greetgo.file_storage.impl;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Supplier;

enum IdGeneratorType {
  STR13(new Supplier<String>() {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String ENG = "abcdefghijklmnopqrstuvwxyz";
    private static final String DEG = "0123456789";
    private final char[] ALL = (ENG.toLowerCase() + ENG.toUpperCase() + DEG).toCharArray();

    @Override
    public String get() {
      final int len = 13;
      char[] ret = new char[len];
      int length = ALL.length;
      for (int i = 0; i < len; i++) {
        ret[i] = ALL[RND.nextInt(length)];
      }
      return new String(ret);
    }
  }),

  HEX12(new Supplier<String>() {
    @Override
    public String get() {
      byte[] bytes12 = new byte[12];
      RND.nextBytes(bytes12);
      return HexUtil.bytesToHex(bytes12);
    }
  });

  private static final Random RND = new SecureRandom();
  final Supplier<String> generator;

  IdGeneratorType(Supplier<String> generator) {
    this.generator = generator;
  }

}
