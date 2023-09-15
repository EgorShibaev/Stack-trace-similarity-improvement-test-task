package org.csc.java.spring2023;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Класс-дескриптор блока, в котором хранится значение.
 * <p>
 * Если вам это потребуется, можете заменить этот record на class.
 */
class FileBlockLocation {
    String fileName;
    int offset;
    int size;

   FileBlockLocation(String fileName, int offset, int size) {
       this.fileName = fileName;
       this.offset = offset;
       this.size = size;
   }


  public void writeTo(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.writeUTF(Path.of(fileName).getFileName().toString());
    dataOutputStream.writeInt(offset);
    dataOutputStream.writeInt(size);
  }

  public static FileBlockLocation readFrom(DataInputStream dataInputStream, Path workingDir)
      throws IOException {
    var fileName = dataInputStream.readUTF();
    var offset = dataInputStream.readInt();
    var size = dataInputStream.readInt();

    return new FileBlockLocation(workingDir.resolve(fileName).toString(), offset, size);
  }
}
