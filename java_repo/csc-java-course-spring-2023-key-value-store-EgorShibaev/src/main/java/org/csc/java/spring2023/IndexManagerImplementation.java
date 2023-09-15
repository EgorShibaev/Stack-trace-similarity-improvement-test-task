package org.csc.java.spring2023;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexManagerImplementation implements IndexManager {

  private final Map<ByteWrapper, List<FileBlockLocation>> indexes;
  private final File indexFile;
  @SuppressWarnings("FieldCanBeLocal")
  private final String indexFileName = "index.txt";

  public IndexManagerImplementation(Path workingDir) throws IOException {
    indexFile = workingDir.resolve(indexFileName).toFile();

    indexes = new HashMap<>();

    if (indexFile.exists()) {
      try (FileInputStream fileInputStream = new FileInputStream(indexFile);
          DataInputStream dataInputStream = new DataInputStream(fileInputStream)
      ) {
        var entries = dataInputStream.readInt();

        for (int entry = 0; entry < entries; ++entry) {
          ByteWrapper key = ByteWrapper.readFrom(fileInputStream);

          var blocks = dataInputStream.readInt();

          List<FileBlockLocation> locations = new ArrayList<>();

          for (int block = 0; block < blocks; ++block) {
            locations.add(FileBlockLocation.readFrom(dataInputStream, workingDir));
          }

          indexes.put(key, locations);
        }
      }
    }
  }

  @Override
  public void add(byte[] key, List<FileBlockLocation> writtenBlocks) {
    indexes.put(new ByteWrapper(key), writtenBlocks);
  }

  @Override
  public void remove(byte[] key) {
    indexes.remove(new ByteWrapper(key));
  }

  @Override
  public List<FileBlockLocation> getFileBlocksLocations(byte[] key) {
    return indexes.get(new ByteWrapper(key));
  }

  @Override
  public void close() throws IOException {
    //noinspection ResultOfMethodCallIgnored
    indexFile.createNewFile();

    try (FileOutputStream fileOutputStream = new FileOutputStream(indexFile);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)
    ) {
      dataOutputStream.writeInt(indexes.size());

      for (var entry : indexes.entrySet()) {
        entry.getKey().writeTo(fileOutputStream);

        dataOutputStream.writeInt(entry.getValue().size());

        for (var loc : entry.getValue()) {
          loc.writeTo(dataOutputStream);
        }
      }
    }
  }
}
