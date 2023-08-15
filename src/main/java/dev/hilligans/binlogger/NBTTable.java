package dev.hilligans.binlogger;

import dev.hilligans.binlogger.util.DataInputByteBuffer;
import dev.hilligans.binlogger.util.DataOutputByteBuffer;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NBTTable {

    public ByteBuffer buffer;
    public ByteBuffer headBuffer;
    public int bufferPointer;
    public int tableSize;
    public int tablePointer;
    RandomAccessFile aFile;

    public NBTTable(Region region) {
        this.tableSize = 256;
        File f = new File(Save.getSaveDirectory(region.worldName, region.x, region.z, "data-") + region.date + "-" + region.count + ".dat");
        try {
            if (f.exists()) {
                aFile = new RandomAccessFile(f, "rw");
                headBuffer = aFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, aFile.length());
                this.tableSize = headBuffer.getInt(8);
                this.bufferPointer = headBuffer.getInt(12);
                this.tablePointer = headBuffer.getInt(16);
                buffer = headBuffer.slice(32, (int) (aFile.length() - 32));
            } else {
                aFile = new RandomAccessFile(f, "rw");
                aFile.setLength((long) MAXNBT * tableSize);
                headBuffer = aFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, aFile.length());
                buffer = headBuffer.slice(32, (int) (aFile.length() - 32));
                saveHeader();
            }
            aFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NBTTable(File file) {
        try {
            try (RandomAccessFile aFile = new RandomAccessFile(file, "r")) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                aFile.getChannel().read(byteBuffer);
                this.tableSize = byteBuffer.getInt(8);
                this.bufferPointer = byteBuffer.getInt(12);
                this.tablePointer = byteBuffer.getInt(16);
                buffer = aFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 32, aFile.length() - 32);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int MAXNBT = 4096 * 8;
    public static final int ENTRY_HEADER_SIZE = 16;

    public int writeNBT(NBTTagReaderWriter readerWriter) {
        int hash = readerWriter.hashCode();
        ByteBuffer buf = null;
        DataOutputByteBuffer data = null;
        for(int x = 0; x < tablePointer; x++) {
            if(getEntryHash(x) == hash) {
                if(buf == null) {
                    buf = MemoryUtil.memAlloc(MAXNBT);
                    data = new DataOutputByteBuffer(buf, 0);
                    try {
                        readerWriter.write(data);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                out: {
                    if (getEntrySize(x) == data.position) {
                        int pos = getEntryPosition(x);
                        int i;
                        for (i = 0; i < data.position / 8; i++) {
                            if (buffer.getLong(i * 8) != buffer.getLong(pos + i * 8)) {
                                break out;
                            }
                        }
                        i*=8;
                        for(; i < data.position; i++) {
                            if (buffer.get(i) != buffer.get(pos + i)) {
                                break out;
                            }
                        }
                        MemoryUtil.memFree(buf);
                        return x;
                    }
                }
            }
        }
        if(buf == null) {
            data = new DataOutputByteBuffer(buffer, bufferPointer);
            try {
                readerWriter.write(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return writeEntryHeader(readerWriter.hashCode(), data.position - bufferPointer);
        } else {
            int length = data.position;
            buffer.put(bufferPointer, buf, 0, length);
            MemoryUtil.memFree(buf);
            return writeEntryHeader(readerWriter.hashCode(), length);
        }
    }

    public void readNBT(int index, NBTTagReaderWriter readerWriter) {
        try {
            readerWriter.read(new DataInputByteBuffer(buffer, buffer.getInt(index * ENTRY_HEADER_SIZE)));
        } catch (Exception e) {
            throw new RuntimeException("Fuck");
        }
    }

    public final int getEntryPosition(int index) {
        return buffer.getInt(index * ENTRY_HEADER_SIZE);
    }

    public final int getEntryHash(int index) {
        return buffer.getInt(index * ENTRY_HEADER_SIZE + 4);
    }

    public final int getEntrySize(int index) {
        return buffer.getInt(index * ENTRY_HEADER_SIZE + 8);
    }

    public boolean hasCapacity(int size) {
        return bufferPointer + size < buffer.capacity();
    }
    public int writeEntryHeader(int hash, int length) {
        int index = tablePointer++;
        buffer.putInt(index * ENTRY_HEADER_SIZE, bufferPointer);
        buffer.putInt(index * ENTRY_HEADER_SIZE + 4, hash);
        buffer.putInt(index * ENTRY_HEADER_SIZE + 8, length);
        return index;
    }

    public void saveHeader() {
        headBuffer.putInt(8, tableSize);
        headBuffer.putInt(12, bufferPointer);
        headBuffer.putInt(16, tablePointer);
    }
}


