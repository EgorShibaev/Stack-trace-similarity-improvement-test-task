package org.csc.java.spring2023;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class KeyValueStoreImplementation implements KeyValueStore {

  private final IndexManager indexManager;
  private boolean closed = false;
  private final ValueStoreManager valueStoreManager;

  public KeyValueStoreImplementation(Path workingDir, int valueFileSize) throws IOException {
    if (valueFileSize <= 0) {
      throw new IllegalArgumentException("Value file size cannot be less than 0");
    }
    Objects.requireNonNull(workingDir);

    if (!Files.exists(workingDir) || !Files.isDirectory(workingDir)) {
      throw new IllegalArgumentException();
    }

    indexManager = new IndexManagerImplementation(workingDir);
    valueStoreManager = new ValueStoreManagerImplementation(workingDir, valueFileSize);

  }

  void check_closed() {
    if (closed) {
      throw new IllegalStateException("Key value store is closed.");
    }
  }

  @Override
  public boolean contains(byte[] key) throws IOException {
    Objects.requireNonNull(key);
    check_closed();

    return indexManager.getFileBlocksLocations(key) != null;
  }

  @Override
  public InputStream openValueStream(byte[] key) throws IOException {
    Objects.requireNonNull(key);
    if (!contains(key)) {
      throw new IOException("No such key in store");
    }
    check_closed();


    var blocks = indexManager.getFileBlocksLocations(key);
    var stream = InputStream.nullInputStream();

    for (var block : blocks) {
      stream = new SequenceInputStream(stream, valueStoreManager.openBlockStream(block));
    }
    return stream;
  }

  @Override
  public byte[] loadValue(byte[] key) throws IOException {
    Objects.requireNonNull(key);
    check_closed();


    var stream = openValueStream(key);
    return stream.readAllBytes();
  }

  @Override
  public void upsert(byte[] key, byte[] value) throws IOException {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    check_closed();


    if (contains(key)) {
      valueStoreManager.remove(indexManager.getFileBlocksLocations(key));
      indexManager.remove(key);
    }
    indexManager.add(key, valueStoreManager.add(value));
  }

  @Override
  public boolean remove(byte[] key) throws IOException {
    Objects.requireNonNull(key);
    check_closed();

    if (contains(key)) {
      valueStoreManager.remove(indexManager.getFileBlocksLocations(key));
      indexManager.remove(key);
      return true;
    }
    return false;
  }

  @Override
  public IndexManager getIndexManager() {
    check_closed();

    return indexManager;
  }

  @Override
  public void close() throws IOException {
    indexManager.close();
    valueStoreManager.close();
    closed = true;
  }
}
