package org.csc.java.spring2023;

import static java.lang.Integer.min;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ValueStoreManagerImplementation implements ValueStoreManager {

  private final Stack<FileBlockLocation> freeBlocksPull;
  private int lastFileNumber = 0;
  private final Path workingDir;
  private final int valueFileSize;
  private final File indexFile;
  @SuppressWarnings("FieldCanBeLocal")
  private final String valueFileIndexName = "ValueIndex.txt";


  public ValueStoreManagerImplementation(Path workingDir, int valueFileSize)
      throws IOException {
    this.workingDir = workingDir;
    freeBlocksPull = new Stack<>();
    this.valueFileSize = valueFileSize;

    indexFile = new File(workingDir.resolve(valueFileIndexName).toString());

    if (indexFile.exists()) {
      FileInputStream fileInputStream = new FileInputStream(indexFile);
      DataInputStream dataInputStream = new DataInputStream(fileInputStream);

      lastFileNumber = dataInputStream.readInt();

      var freeBlocks = dataInputStream.readInt();

      for (int i = 0; i < freeBlocks; ++i) {
        freeBlocksPull.add(FileBlockLocation.readFrom(dataInputStream, workingDir));
      }

      fileInputStream.close();
      dataInputStream.close();
    }
  }

  @Override
  public List<FileBlockLocation> add(byte[] value) throws IOException {
    var blocks = new ArrayList<FileBlockLocation>();

    int written = 0;
    while (!freeBlocksPull.empty() && written < value.length) {
      var block = freeBlocksPull.pop();

      var file = new RandomAccessFile(block.fileName, "rw");
      file.seek(block.offset);
      int bytesToWrite = min(value.length - written, block.size);
      file.write(value, written, bytesToWrite);
      file.close();
      blocks.add(new FileBlockLocation(block.fileName, block.offset, bytesToWrite));

      if (block.size > bytesToWrite) {
        freeBlocksPull.add(new FileBlockLocation(
            block.fileName,
            block.offset + bytesToWrite,
            block.size - bytesToWrite)
        );
      }

      written += bytesToWrite;
    }

    while (written < value.length) {
      String filesPrefix = "data";
      var newBlock = new FileBlockLocation(
          workingDir.resolve(filesPrefix + lastFileNumber).toString(),
          0,
          min(value.length - written, valueFileSize)
      );
      lastFileNumber++;
      var file = new File(newBlock.fileName);
      if (!file.createNewFile()) {
        if (!file.delete()) {
          throw new IllegalStateException();
        }
        if (!file.createNewFile()) {
          throw new IllegalStateException();
        }
      }
      var fileOutputStream = new FileOutputStream(newBlock.fileName);
      fileOutputStream.write(value, written, newBlock.size);
      fileOutputStream.close();

      written += newBlock.size;

      blocks.add(newBlock);
    }
    return blocks;
  }

  @Override
  public InputStream openBlockStream(FileBlockLocation location) throws IOException {
    RandomAccessFile file = new RandomAccessFile(location.fileName, "r");
    file.seek(location.offset);

    return new InputStream() {

      int bytesLeft = location.size;

      @Override
      public int read() throws IOException {
        if (bytesLeft == 0) {
          file.close();
          return -1;
        }
        bytesLeft--;
        return file.read();
      }
    };
  }

  @Override
  public void remove(List<FileBlockLocation> valueBlocksLocations) {
    freeBlocksPull.addAll(valueBlocksLocations);
  }

  @Override
  public void close() throws IOException {
    //noinspection ResultOfMethodCallIgnored
    indexFile.createNewFile();

    try (FileOutputStream fileOutputStream = new FileOutputStream(indexFile);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)
    ) {
      dataOutputStream.writeInt(lastFileNumber);

      dataOutputStream.writeInt(freeBlocksPull.size());

      while (!freeBlocksPull.empty()) {
        freeBlocksPull.pop().writeTo(dataOutputStream);
      }

    }
  }
}
