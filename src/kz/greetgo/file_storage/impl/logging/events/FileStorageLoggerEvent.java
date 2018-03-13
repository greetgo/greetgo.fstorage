package kz.greetgo.file_storage.impl.logging.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static kz.greetgo.file_storage.impl.logging.events.FileStorageLoggerEventsUtil.doubleToStr;

public interface FileStorageLoggerEvent {
  Date happenedAt();

  String sql();

  List<Object> params();

  long delayInNanos();

  default double delayInSeconds() {
    return (double) delayInNanos() / 1_000_000_000.0;
  }

  default void appendMoreInfo(List<String> infoList) {}

  default List<String> info() {
    List<String> ret = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    ret.add("[" + sdf.format(happenedAt()) + "] " + getClass().getSimpleName()
      + ", delay = " + doubleToStr(delayInSeconds()) + "sec");
    int i = 1;

    List<Object> params = params();
    if (params.size() == 0) {
      ret.add("No params");
    } else for (Object param : params) {
      ret.add("  param " + i++ + " = " + (param == null ? "< NULL >" : param));
    }
    appendMoreInfo(ret);
    ret.add("SQL: " + sql());
    return ret;
  }
}
