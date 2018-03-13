package kz.greetgo.file_storage.impl.logging.events;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FileStorageLoggerEventsUtil {
  public static String doubleToStr(double value) {
    if (value < 1e-7) return ("" + value).toLowerCase();
    DecimalFormat df = new DecimalFormat("#0.#######");
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    symbols.setGroupingSeparator(' ');
    df.setDecimalFormatSymbols(symbols);
    return df.format(value);
  }
}
