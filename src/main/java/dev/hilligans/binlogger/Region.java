package dev.hilligans.binlogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;

public class Region {

    long lastTime = 0;

    ByteBuffer byteBuffer;
    int bufferPointer = 0;
    public int bufferSize = 0;
    public PlayerTable playerTable = new PlayerTable();

    public int x;
    public int z;
    public int protocolVersion;

    public String worldName;
    public String date;
    public int count = 0;
    public long startTime;
    public long endTime;

    public long actionChecksum;

    public int timeHeaderOffset;


    public static final int TIME_HEADER_OFFSET = 64;
    public static final int REGION_WIDTH = 1 << 11;
    public static final int REGION_HEIGHT = 1 << 10;

    public Region(int x, int z, String worldName, int protocolVersion, long actionChecksum) {
        this.x = x;
        this.z = z;
        this.worldName = worldName;
        this.date = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().toString();
        this.startTime = System.currentTimeMillis();
        this.timeHeaderOffset = TIME_HEADER_OFFSET;
        this.protocolVersion = protocolVersion;
        this.actionChecksum = actionChecksum;
        this.bufferSize = 1000000000;
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
    }

    public Region(Region region, int size, PlayerTable playerTable) {
        this.x = region.x;
        this.z = region.z;
        this.worldName = region.worldName;
        this.date = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().toString();
        this.startTime = System.currentTimeMillis();
        this.timeHeaderOffset = TIME_HEADER_OFFSET;
        this.protocolVersion = region.protocolVersion;
        this.actionChecksum = region.actionChecksum;
        this.bufferSize = size;
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.playerTable = playerTable;
    }

    public Region(ByteBuffer byteBuffer) {
        readHeader(byteBuffer);
        System.out.println("New region x " + x + " z " + z + " worldName " + worldName);
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.byteBuffer.mark();
        this.byteBuffer.put(0, byteBuffer, timeHeaderOffset * 16, byteBuffer.limit() - timeHeaderOffset * 16);
        this.byteBuffer.reset();
    }

    public Region(FileChannel fileChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(TIME_HEADER_OFFSET * 16);
            buffer.mark();
            fileChannel.read(buffer);
            buffer.reset();
            readHeader(buffer);
            this.byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, timeHeaderOffset * 16L, fileChannel.size() - timeHeaderOffset * 16L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Region createRegion(File file) throws IOException {
        try(RandomAccessFile aFile = new RandomAccessFile(file, "rw")) {
            int length = (int) aFile.length();
            ByteBuffer buf = ByteBuffer.allocate(length);
            buf.mark();
            aFile.getChannel().read(buf);
            buf.reset();
            aFile.close();
            return new Region(buf);
        }
    }

    public static Region createMappedRegion(File file) {
        try {
            try (RandomAccessFile aFile = new RandomAccessFile(file, "r")) {
                return new Region(aFile.getChannel());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ByteBuffer getBufferForReading() {
        return byteBuffer;
    }

    public int getBufferPointer() {
        return bufferPointer;
    }

    public int getTimeHeaderOffset() {
        return timeHeaderOffset;
    }

    public long getSizeInBytes() {
        return timeHeaderOffset * 16L + bufferPointer * 16L;
    }

    public void write(Action action, short owner, int x, int y, int z, long time, long data) {
        long convertedTime = (time - lastTime) * 20 / 1000;
        if(bufferPointer % timeHeaderOffset == 0 || convertedTime > (1 << 16)) {
            writeTimestamp(time);
            convertedTime = (time - lastTime) * 1000 / 20;
        }
        write((long)action.getID() << 6 * 8 | ((long)(owner & 0xFFFF) << 4 * 8) | (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL)), (long)((short)convertedTime) << 6 * 8 | data);
    }

    public void write(long long1, long time, long data) {
        long convertedTime = (time - lastTime) * 20 / 1000;
        if(bufferPointer % timeHeaderOffset == 0 || convertedTime > (1 << 16)) {
            writeTimestamp(time);
            convertedTime = (time - lastTime) * 1000 / 20;
        }
        write(long1, (long)((short)convertedTime) << 6 * 8 | data);
    }

    private synchronized void write(long long1, long long2) {
        byteBuffer.putLong(bufferPointer * 16, long1);
        byteBuffer.putLong(bufferPointer * 16 + 8, long2);
        bufferPointer++;
    }

    public int query(Action action_, Short owner_, Integer x_, Integer y_, Integer z_, Long timeStart_, Long timeEnd_, int[] data, int dataOffset) {
        short action = action_ == null ? 0 : action_.getID();
        short owner = owner_ == null ? 0 : owner_;
        int pos = x_ == null ? 0 : (int) (((x_ & 0x7FFL) << 21) | ((z_ & 0x7FFL) << 10) | (y_ & 0x3FFL));
        long startTime = (timeStart_ == null ? 0 : timeStart_);
        long timeEnd = (timeEnd_ == null ? 0 : timeEnd_);

        int offset = 0;
        int x = 0;
        int requiredCount = (action_ == null ? 0 : 1) + (owner_ == null ? 0 : 1) + (x_ == null ? 0 : 1) + (timeStart_ == null ? 0 : 1) + (timeEnd_ == null ? 0 : 1);

        if(timeStart_ != null) {
            while(bufferPointer > x) {

                if ((byteBuffer.getLong(x * 16) & 0xFFFF000000000000L) == (1L << 48L)) {
                    if(byteBuffer.getLong(x * 16 + 8) > startTime) {
                        x -= timeHeaderOffset;
                        if(x < 0) {
                            x = 0;
                        }
                        break;
                    }
                }
                x += timeHeaderOffset;
            }
        }
        long timeHeader = 0;
        for(; x < bufferPointer; x++) {
            if(x % timeHeaderOffset == 0) {
                timeHeader = byteBuffer.getLong(x * 16 + 8);
                continue;
            }
            int p = x * 16;
            int count = 0;
            if(action_ != null && byteBuffer.getShort(p) == action) {
                count++;
            }
            if(owner_ != null && byteBuffer.getShort(p + 2) == owner) {
                count++;
            }
            if(x_ != null && byteBuffer.getInt(p + 4) == pos) {
                count++;
            }

            long time = calculateTime(byteBuffer.getShort(p + 8), timeHeader);
            if(timeStart_ != null && time > startTime) {
                count++;
            }
            if(timeEnd_ != null && time < timeEnd) {
                count++;
            } else if(timeEnd_ != null && time > timeEnd) {
                break;
            }

            if(count == requiredCount) {
                data[offset + dataOffset] = x;
                offset++;
                if(offset + dataOffset == data.length) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public PlayerTable getPlayerTable() {
        return playerTable;
    }

    public static long calculateTime(short ticks, long time) {
        return time + ticks * 1000 / 20;
    }

    public long getTimestamp(int index) {
        index -= index % timeHeaderOffset;
        return byteBuffer.getLong(index * 16 + 8);
    }

    public Database.DatabaseEntry getEntry(int index) {
        return new Database.DatabaseEntry(this, getTimestamp(index), byteBuffer.getLong(index * 16), byteBuffer.getLong(index * 16 + 8));
    }

    public void writeTimestamp(long time) {
        this.lastTime = time;
        bufferPointer += bufferPointer % timeHeaderOffset == 0 ? 0 : timeHeaderOffset - bufferPointer % timeHeaderOffset;
        byteBuffer.putLong(bufferPointer * 16, (long)Action.TIME_HEADER.getID() << 6 * 8);
        byteBuffer.putLong(bufferPointer * 16 + 8, time);
        bufferPointer++;
    }

    public void readHeader(ByteBuffer buffer) {
        buffer.getLong();
        buffer.getLong();

        buffer.getShort();
        this.timeHeaderOffset = buffer.getInt();
        int c = buffer.getShort();
        this.x = buffer.getInt();
        this.z = buffer.getInt();

        buffer.getShort();
        this.protocolVersion = buffer.getInt();
        this.startTime = buffer.getLong();
        buffer.getShort();

        buffer.getShort();
        this.endTime = buffer.getLong();
        this.count = buffer.getInt();
        buffer.getShort();

        buffer.getShort();
        this.bufferPointer = buffer.getInt();
        this.actionChecksum = buffer.getLong();
        buffer.getShort();

        buffer.getShort();
        this.bufferSize = buffer.getInt();
        buffer.getLong();
        int worldLength = buffer.getShort();

        StringBuilder stringBuilder = new StringBuilder(worldLength);

        int ptr = 0;
        while(worldLength > ptr) {
            buffer.getShort();
            for(int x = 0; x < 14 && worldLength > ptr; x++) {
                stringBuilder.append((char)buffer.get());
                ptr++;
            }
        }
        this.worldName = stringBuilder.toString();

        this.date = Instant.ofEpochMilli(startTime).atZone(ZoneId.of("UTC")).toLocalDate().toString();
    }

    public void writeHeader(FileChannel fileChannel) throws IOException {
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(timeHeaderOffset * 16);

        byteBuffer1.putShort((short) 0);
        byteBuffer1.put("Binary Logger.".getBytes(StandardCharsets.US_ASCII));

        byteBuffer1.putShort((short) 0);
        byteBuffer1.putInt(timeHeaderOffset);
        //Time header offset count of table, will probably always be one
        byteBuffer1.putShort((short) 1);
        byteBuffer1.putInt(x);
        byteBuffer1.putInt(z);

        byteBuffer1.putShort((short) 0);
        byteBuffer1.putInt(protocolVersion);
        byteBuffer1.putLong(startTime);
        byteBuffer1.putShort((short) 0);

        byteBuffer1.putShort((short) 0);
        byteBuffer1.putLong(endTime);
        byteBuffer1.putInt(count);
        byteBuffer1.putShort((short) 0);

        byteBuffer1.putShort((short) 0);
        byteBuffer1.putInt(bufferPointer);
        byteBuffer1.putLong(actionChecksum);
        byteBuffer1.putShort((short)0);

        byteBuffer1.putShort((short)0);
        byteBuffer1.putInt(bufferSize);
        byteBuffer1.putLong(0);
        byteBuffer1.putShort((short) worldName.length());

        int ptr = 0;
        while((short)worldName.length() > ptr) {
            byteBuffer1.putShort((short) 0);
            for (int x = 0; x < 14 && (short) worldName.length() > ptr; x++) {
                byteBuffer1.put((byte) worldName.charAt(ptr));
                ptr++;
            }
        }

        byteBuffer1.flip();
        byteBuffer1.limit(timeHeaderOffset * 16);
        fileChannel.write(byteBuffer1);
        byteBuffer1.flip();
    }

    public void save() {
        try {
            File f = new File(Save.getSaveDirectory(worldName, x, z));
            if(!f.exists()) {
                if(!f.mkdirs()) {
                    throw new RuntimeException("Failed to create save folder");
                }
            }

            try(RandomAccessFile aFile = new RandomAccessFile(Save.getSaveDirectory(worldName, x, z) + date + "-" + count + ".dat", "rw")) {
                FileChannel inChannel = aFile.getChannel();
                writeHeader(inChannel);
                inChannel.write(byteBuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
