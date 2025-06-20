package dev.hilligans.binlogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;

public class Region implements Iterable<Integer> {

    long lastTime = 0;

    ByteBuffer byteBuffer;
    int bufferPointer = 0;
    public int bufferSize = 0;
    public PlayerTable playerTable;
    public NBTTable nbtTable;
    public RandomAccessFile aFile;
    public FileChannel fileChannel;


    public int x;
    public int z;
    public int protocolVersion;

    public String worldName;
    public String date;
    public int count = 0;
    public long startTime;
    public long endTime;
    public boolean writing = true;

    public long actionChecksum;

    public int timeHeaderOffset;

    public boolean loaded = false;


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
        this.bufferSize = 10000000;

        load();
    }

    public Region(String filePath) {

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

    public void load() {
        try {
            File f = new File(Save.getSaveDirectory(worldName, x, z, "log-") + date + "-" + count + ".dat");
            f.getParentFile().mkdirs();
            boolean exists = f.exists();

            aFile = new RandomAccessFile(f, "rw");
            fileChannel = aFile.getChannel();
            if(exists) {
                System.out.println("File Exists");
                ByteBuffer buffer = ByteBuffer.allocate(TIME_HEADER_OFFSET * 16);
                buffer.mark();
                fileChannel.read(buffer);
                buffer.reset();
                readHeader(buffer);
            } else {
                aFile.setLength(bufferSize);
                writeHeader(fileChannel);
            }
            this.byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, timeHeaderOffset * 16L, fileChannel.size() - timeHeaderOffset * 16L);
            byteBuffer = byteBuffer.slice(16, byteBuffer.limit() - 16);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        playerTable = new PlayerTable(this);
        nbtTable = new NBTTable(this);
        loaded = true;
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

    public void write(Action action, short owner, int x, int y, int z, long time, long data, boolean hidden) {
        long convertedTime = (time - lastTime) * 20 / 1000;
        if(bufferPointer % timeHeaderOffset == 0 || convertedTime > (1 << 16)) {
            writeTimestamp(time);
            convertedTime = (time - lastTime) * 1000 / 20;
        }
        write((((long)action.getID() << 6 * 8) & ~(1L << 63)) | ((long)(owner & 0xFFFF) << 4 * 8) | (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL)) | (hidden ? 1L << 63 : 0), (long)((short)convertedTime) << 6 * 8 | data);
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

    public int query(Query query, int[] data, int dataOffset, int limit) {
        if(query.getProperty(Query.PLAYER)) {
            query.withUser(playerTable.getPlayer(query.player));
        }
        if(query.getProperty(Query.POSITION)) {
            //check if the position is in the region
        }

        return switch (query.properties & 0b11111) {
            case Query.ACTION -> Queries.queryForAction(this, query.action, data, dataOffset, limit);
            case Query.USER -> Queries.queryForOwner(this, query.user, data, dataOffset, limit);
            case Query.ACTION | Query.USER -> Queries.queryForActionOwner(this, query.action, query.user, data, dataOffset, limit);
            case Query.POSITION -> Queries.queryForPosition(this, query.x, query.y, query.z, data, dataOffset, limit);
            case Query.ACTION | Query.POSITION -> Queries.queryForActionPosition(this, query.action, query.x, query.y, query.z, data, dataOffset, limit);
            case Query.USER | Query.POSITION -> Queries.queryForUserPosition(this, query.user, query.x, query.y, query.z, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.POSITION -> Queries.queryForActionUserPosition(this, query.action, query.user, query.x, query.y, query.z, data, dataOffset, limit);
            case Query.TIME -> Queries.queryForTime(this, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.ACTION | Query.TIME -> Queries.queryForActionTime(this, query.action, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.USER | Query.TIME -> Queries.queryForOwnerTime(this, query.user, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.TIME -> Queries.queryForActionOwnerTime(this, query.action, query.user, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.POSITION | Query.TIME -> Queries.queryForPositionTime(this, query.x, query.y, query.z, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.ACTION | Query.POSITION | Query.TIME -> Queries.queryForActionPositionTime(this, query.action, query.x, query.y, query.z, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.USER | Query.POSITION | Query.TIME -> Queries.queryForUserPositionTime(this, query.user, query.x, query.y, query.z, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.POSITION | Query.TIME -> Queries.queryForActionUserPositionTime(this, query.action, query.user, query.x, query.y, query.z, query.startTime, query.endTime, data, dataOffset, limit);
            case Query.DATA -> Queries.queryForData(this, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.DATA -> Queries.queryForActionData(this, query.action, query.data, query.dataMask, data, dataOffset, limit);
            case Query.USER | Query.DATA -> Queries.queryForOwnerData(this, query.user, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.DATA -> Queries.queryForActionOwnerData(this, query.action, query.user, query.data, query.dataMask, data, dataOffset, limit);
            case Query.POSITION | Query.DATA -> Queries.queryForPositionData(this, query.x, query.y, query.z, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.POSITION | Query.DATA -> Queries.queryForActionPositionData(this, query.action, query.x, query.y, query.z, query.data, query.dataMask, data, dataOffset, limit);
            case Query.USER | Query.POSITION | Query.DATA -> Queries.queryForUserPositionData(this, query.user, query.x, query.y, query.z, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.POSITION | Query.DATA -> Queries.queryForActionUserPositionData(this, query.action, query.user, query.x, query.y, query.z, query.data, query.dataMask, data, dataOffset, limit);
            case Query.TIME | Query.DATA -> Queries.queryForTimeData(this, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.TIME | Query.DATA -> Queries.queryForActionTimeData(this, query.action, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.USER | Query.TIME | Query.DATA -> Queries.queryForOwnerTimeData(this, query.user, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.TIME | Query.DATA -> Queries.queryForActionOwnerTimeData(this, query.action, query.user, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.POSITION | Query.TIME | Query.DATA -> Queries.queryForPositionTimeData(this, query.x, query.y, query.z, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.POSITION | Query.TIME | Query.DATA -> Queries.queryForActionPositionTimeData(this, query.action, query.x, query.y, query.z, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.USER | Query.POSITION | Query.TIME | Query.DATA -> Queries.queryForUserPositionTimeData(this, query.user, query.x, query.y, query.z, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);
            case Query.ACTION | Query.USER | Query.POSITION | Query.TIME | Query.DATA -> Queries.queryForActionUserPositionTimeData(this, query.action, query.user, query.x, query.y, query.z, query.startTime, query.endTime, query.data, query.dataMask, data, dataOffset, limit);

            default -> 0;
        };
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

    public NBTTable getNBTTable() {
        return nbtTable;
    }

    public static long calculateTime(short ticks, long time) {
        return time + ticks * 1000 / 20;
    }

    public long getTimestamp(int index) {
        index -= index % timeHeaderOffset;
        return byteBuffer.getLong(index * 16 + 8);
    }

    public long getTime(int index) {
        return calculateTime(byteBuffer.getShort(index * 16 + 8), getTimestamp(index));
    }

    public Database.DatabaseEntry getEntry(int index) {
        return new Database.DatabaseEntry(this, getTimestamp(index), byteBuffer.getLong(index * 16), byteBuffer.getLong(index * 16 + 8));
    }

    public void convertAction(int[] conversionTable) {
        for (int val : this) {
            short header = (byteBuffer.getShort(val));
            short newHeader = (short) (conversionTable[(header & ~(1 << 15))] | (header & (1 << 15)));
            byteBuffer.putShort(val, newHeader);
        }
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
        this.lastTime = buffer.getLong();
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
        System.out.println(this);
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
        byteBuffer1.putLong(lastTime);
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
            File f = new File(Save.getSaveDirectory(worldName, x, z, "log-"));
            if(!f.exists()) {
                if(!f.mkdirs()) {
                    throw new RuntimeException("Failed to create save folder");
                }
            }
            try(RandomAccessFile aFile = new RandomAccessFile(Save.getSaveDirectory(worldName, x, z, "log-") + date + "-" + count + ".dat", "rw")) {
                FileChannel inChannel = aFile.getChannel();
                writeHeader(inChannel);
                inChannel.write(byteBuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveHeader() {
        if(fileChannel != null) {
            try {
                writeHeader(fileChannel.position(0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(nbtTable != null) {
            nbtTable.saveHeader();
        }
    }

    public void close() {
        try {
            if (fileChannel != null) {
                writeHeader(fileChannel);
                aFile.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<Integer> iterator() {
        return new Iterator<>() {

            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < bufferPointer;
            }

            @Override
            public Integer next() {
                int val = pos;
                pos++;
                if(pos % timeHeaderOffset == 0) {
                    pos++;
                }
                return val * 16;
            }
        };
    }

    @Override
    public String toString() {
        return "Region{" +
                "lastTime=" + lastTime +
                ", bufferPointer=" + bufferPointer +
                ", bufferSize=" + bufferSize +
                ", x=" + x +
                ", z=" + z +
                ", protocolVersion=" + protocolVersion +
                ", worldName='" + worldName + '\'' +
                ", date='" + date + '\'' +
                ", count=" + count +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", writing=" + writing +
                ", actionChecksum=" + actionChecksum +
                ", timeHeaderOffset=" + timeHeaderOffset +
                '}';
    }
}
