package dev.hilligans.binlogger.data;

import java.lang.foreign.MemorySegment;

public class Region extends BinaryFile implements AutoCloseable {

    public static final long STARTING_REGION_SIZE = 4096;


    public String path;
    public boolean loaded = false;


    /* Begin of header data */
    public static final String HEADER = "Binary logger 2.";

    /* fileID */
    /* gameVersion */
    /* if time header is 0, we are working with compressed data and there's no repeated time header. */
    public int timeHeaderOffset;

    public int x;
    public int y;
    public int z;
    public int logCount;

    public long startTime;
    public long endTime;


    Segment segment;


    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public long getWantedSize() {
        return STARTING_REGION_SIZE;
    }

    @Override
    public void readHeader(Segment segment) {
        String s = segment.readString();
        if(!s.equals(HEADER)) {
            throw new RuntimeException("invalid header " + s + " for region " + path);
        }

        fileID = segment.readLong();
        gameVersion = segment.readInt();
        timeHeaderOffset = segment.readInt();

        x = segment.readInt();
        y = segment.readInt();
        z = segment.readInt();
        logCount = segment.readInt();

        startTime = segment.readLong();
        endTime = segment.readLong();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void setBackingSegment(Segment segment) {
        this.segment = segment;
    }

    @Override
    public void writeHeader(SegmentTable segment) {
        segment.writeString(HEADER);

        segment.writeLong(fileID);
        segment.writeInt(gameVersion);
        segment.writeInt(timeHeaderOffset);

        segment.writeInt(x);
        segment.writeInt(y);
        segment.writeInt(z);
        segment.writeInt(logCount);

        segment.writeLong(startTime);
        segment.writeLong(endTime);


    }

    @Override
    public void close() throws Exception {
        if(segment != null) {
            segment.close();
        }
    }
}
