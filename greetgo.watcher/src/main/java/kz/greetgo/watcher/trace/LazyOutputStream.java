package kz.greetgo.watcher.trace;

import java.io.IOException;
import java.io.OutputStream;

public abstract class LazyOutputStream extends OutputStream {
  private OutputStream out;
  private long count;
  
  protected abstract OutputStream newOut() throws IOException;
  
  private final void ensureOut() throws IOException {
    if (out == null) {
      out = newOut();
      count = 0;
    }
  }
  
  public final void reset() throws IOException {
    if (out == null) return;
    out.close();
    out = null;
  }
  
  public final long getCount() {
    return count;
  }
  
  public final void write(int b) throws IOException {
    ensureOut();
    out.write(b);
    count++;
  }
  
  public final void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }
  
  public final void write(byte[] b, int off, int len) throws IOException {
    if ((off | len | (b.length - (len + off)) | (off + len)) < 0) throw new IndexOutOfBoundsException();
    
    ensureOut();
    out.write(b, off, len);
    count += len;
  }
  
  public final void flush() throws IOException {
    //TODO fix it
    //    if (out != null) {
    //      out.flush();
    //    }
  }
  
  public final void close() throws IOException {
    if (out != null) {
      try {
        out.flush();
      } catch (IOException ignored) {}
      out.close();
    }
  }
}
