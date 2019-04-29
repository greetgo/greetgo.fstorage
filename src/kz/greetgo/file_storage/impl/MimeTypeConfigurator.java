package kz.greetgo.file_storage.impl;

import kz.greetgo.file_storage.errors.UnknownMimeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configurator to manage mime types
 */
public class MimeTypeConfigurator implements FileStorageBuilderConfigurator {

  private final Set<String> mimeTypeOk = new HashSet<>();
  private final Map<String, String> extensionMimeType = new HashMap<>();

  public void registerBaseMimeTypes() {
    registerMimeType(".txt   ~ Text file                       ~ text/plain                                   ");

    registerMimeType(".aac   ~ AAC audio file                  ~ audio/aac                                    ");
    registerMimeType(".abw   ~ AbiWord document                ~ application/x-abiword                        ");
    registerMimeType(".avi   ~ AVI: Audio Video Interleave     ~ video/x-msvideo                              ");
    registerMimeType(".azw   ~ Amazon Kindle eBook format      ~ application/vnd.amazon.ebook                 ");
    registerMimeType(".bin   ~ Any kind of binary data         ~ application/octet-stream                     ");
    registerMimeType(".bz    ~ BZip archive                    ~ application/x-bzip                           ");
    registerMimeType(".bz2   ~ BZip2 archive                   ~ application/x-bzip2                          ");
    registerMimeType(".csh   ~ C-Shell script                  ~ application/x-csh                            ");
    registerMimeType(".css   ~ Cascading Style Sheets (CSS)    ~ text/css                                     ");
    registerMimeType(".csv   ~ Comma-separated values (CSV)    ~ text/csv                                     ");
    registerMimeType(".doc   ~ Microsoft Word                  ~ application/msword                           ");
    registerMimeType(".eot   ~ MS Embedded OpenType fonts      ~ application/vnd.ms-fontobject                ");
    registerMimeType(".epub  ~ Electronic publication (EPUB)   ~ application/epub+zip                         ");
    registerMimeType(".ics   ~ iCalendar format                ~ text/calendar                                ");
    registerMimeType(".jar   ~ Java Archive (JAR)              ~ application/java-archive                     ");
    registerMimeType(".js    ~ JavaScript (ECMAScript)         ~ application/javascript                       ");
    registerMimeType(".json  ~ JSON format                     ~ application/json                             ");
    registerMimeType(".mpeg  ~ MPEG Video                      ~ video/mpeg                                   ");
    registerMimeType(".mpkg  ~ Apple Installer Package         ~ application/vnd.apple.installer+xml          ");
    registerMimeType(".oga   ~ OGG audio                       ~ audio/ogg                                    ");
    registerMimeType(".ogv   ~ OGG video                       ~ video/ogg                                    ");
    registerMimeType(".ogx   ~ OGG                             ~ application/ogg                              ");
    registerMimeType(".otf   ~ OpenType font                   ~ font/otf                                     ");
    registerMimeType(".ppt   ~ Microsoft PowerPoint            ~ application/vnd.ms-powerpoint                ");
    registerMimeType(".rar   ~ RAR archive                     ~ application/x-rar-compressed                 ");
    registerMimeType(".rtf   ~ Rich Text Format (RTF)          ~ application/rtf                              ");
    registerMimeType(".sh    ~ Bourne shell script             ~ application/x-sh                             ");
    registerMimeType(".tar   ~ Tape Archive (TAR)              ~ application/x-tar                           ");
    registerMimeType(".ts    ~ Typescript file                 ~ video/vnd.dlna.mpeg-tts                      ");
    registerMimeType(".ttf   ~ TrueType Font                   ~ font/ttf                                     ");
    registerMimeType(".vsd   ~ Microsoft Visio                 ~ application/vnd.visio                        ");
    registerMimeType(".wav   ~ Waveform Audio Format           ~ audio/x-wav                                  ");
    registerMimeType(".weba  ~ WEBM audio                      ~ audio/webm                                   ");
    registerMimeType(".webm  ~ WEBM video                      ~ video/webm                                   ");
    registerMimeType(".webp  ~ WEBP image                      ~ image/webp                                   ");
    registerMimeType(".woff  ~ Web Open Font Format (WOFF)     ~ font/woff                                    ");
    registerMimeType(".woff2 ~ Web Open Font Format (WOFF)     ~ font/woff2                                   ");
    registerMimeType(".xhtml ~ XHTML                           ~ application/xhtml+xml                        ");
    registerMimeType(".xls   ~ Microsoft Excel                 ~ application/vnd.ms-excel                     ");
    registerMimeType(".xml   ~ XML                             ~ application/xml                              ");
    registerMimeType(".xul   ~ XUL                             ~ application/vnd.mozilla.xul+xml              ");
    registerMimeType(".zip   ~ ZIP archive                     ~ application/zip                              ");
    registerMimeType(".7z    ~ 7-zip archive                   ~ application/x-7z-compressed                  ");

    registerMimeType(".pdf   ~ Adobe Portable Document Format (PDF)      ~ application/pdf                    ");

    registerMimeType(".png       ~ Portable Network Graphics             ~ image/png                          ");
    registerMimeType(".ico       ~ Icon format                           ~ image/x-icon                       ");
    registerMimeType(".svg       ~ Scalable Vector Graphics (SVG)        ~ image/svg+xml                      ");
    registerMimeType(".gif       ~ Graphics Interchange Format (GIF)     ~ image/gif                          ");
    registerMimeType(".tif .tiff ~ Tagged Image File Format (TIFF)       ~ image/tiff                         ");
    registerMimeType(".jpeg .jpg ~ JPEG images                           ~ image/jpeg                         ");

    registerMimeType(".htm .html ~ HyperText Markup Language (HTML)            ~ text/html     ");
    registerMimeType(".mid .midi ~ Musical Instrument Digital Interface (MIDI) ~ audio/midi    ");

    registerMimeType(".3gp ~ 3GPP audio/video container  ~ video/3gpp                                   ");
    registerMimeType(".3gp ~ 3GPP audio/video container  ~ audio/3gpp    ~if it doesn't contain video   ");
    registerMimeType(".3g2 ~ 3GPP2 audio/video container ~ video/3gpp2                                  ");
    registerMimeType(".3g2 ~ 3GPP2 audio/video container ~ audio/3gpp2   ~if it doesn't contain video   ");

    registerMimeType(".odp ~ OpenDocument presentation document~application/vnd.oasis.opendocument.presentation  ");
    registerMimeType(".ods ~ OpenDocument spreadsheet document~application/vnd.oasis.opendocument.spreadsheet    ");
    registerMimeType(".odt ~ OpenDocument text document~application/vnd.oasis.opendocument.text                  ");
    registerMimeType(".arc ~ Archive document (multiple files embedded)~application/octet-stream                 ");
    registerMimeType(".swf ~ Small web format (SWF) or Adobe Flash document~application/x-shockwave-flash        ");

  }

  /**
   * <p>Registers new mimeType</p>
   * <p>
   * Example:
   * <code>
   * registerMimeType(".jpeg .jpg ~ JPEG images ~ image/jpeg ");
   * </code>
   * </p>
   *
   * @param line tilda separated mimeType information: space separated extensions list (started with dots) ~ description ~ mime type
   */
  public void registerMimeType(String line) {
    String mimeType = line.split("~")[2].trim();
    String[] extensions = line.split("~")[0].trim().split("\\s+");

    mimeTypeOk.add(mimeType);

    for (String extension : extensions) {
      if (extension.startsWith(".")) {
        extension = extension.substring(1);
      }
      extensionMimeType.put(extension, mimeType);
    }
  }

  @Override
  public void configure(FileStorageBuilder fileStorageBuilder) {
    fileStorageBuilder.mimeTypeExtractor(this::extractMimeType);
    fileStorageBuilder.mimeTypeValidator(this::validator);
  }

  private Boolean validator(String mimeType) {
    if (mimeType == null) return true;
    if (!mimeTypeOk.contains(mimeType)) {
      throw new UnknownMimeType(
        mimeType,
        "No MIME type '" + mimeType + "' in " + mimeTypeOk.stream().sorted().collect(Collectors.toList())
      );
    }
    return true;
  }

  private String extractMimeType(String fileName) {
    String extension = LocalUtil.extractExtension(fileName);
    return extensionMimeType.get(extension);
  }
}
