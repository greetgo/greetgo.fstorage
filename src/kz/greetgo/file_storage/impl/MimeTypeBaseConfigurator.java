package kz.greetgo.file_storage.impl;

/**
 * @deprecated use {@link MimeTypeConfigurator}
 */
@Deprecated()
public class MimeTypeBaseConfigurator extends MimeTypeConfigurator {

  private MimeTypeBaseConfigurator() {
    registerBaseMimeTypes();
  }

  private enum Ins {
    VALUE;

    final MimeTypeBaseConfigurator instance = new MimeTypeBaseConfigurator();
  }

  public static MimeTypeConfigurator get() {
    return MimeTypeBaseConfigurator.Ins.VALUE.instance;
  }

}
