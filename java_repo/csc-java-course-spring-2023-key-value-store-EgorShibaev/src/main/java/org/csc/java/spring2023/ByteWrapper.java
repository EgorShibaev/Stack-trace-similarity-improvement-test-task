package org.csc.java.spring2023;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Вспомогательная обертка над массивом байтов, понадобится для хранения Map<ByteWrapper,
 * List<FileBlockLocation>> в {@link IndexManager}
 */
final class ByteWrapper {

  private final byte[] data;

  ByteWrapper(byte[] data) {
    this.data = data;
  }

  byte[] getBytes() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    return o instanceof ByteWrapper && Arrays.equals(data, ((ByteWrapper)o).data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }

  public static ByteWrapper readFrom(InputStream reader) throws IOException {
    var len = reader.read();
    byte[] data = new byte[len];
    var charsRead = reader.read(data);

    if (charsRead != len) {
      throw new IOException("Corrupted index file");
    }

    return new ByteWrapper(data);
  }

  public void writeTo(OutputStream outputStream) throws IOException {
    outputStream.write(data.length);
    outputStream.write(data);
  }
}