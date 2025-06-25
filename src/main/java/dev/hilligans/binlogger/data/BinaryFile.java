package dev.hilligans.binlogger.data;

import dev.hilligans.binlogger.util.FileLoader;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public abstract class BinaryFile {

    protected String path;
    protected boolean loaded;
    protected long fileID;
    protected int gameVersion;



    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public int getHeaderSize() {
        return 16 * 64;
    }

    public abstract long getWantedSize();

    public abstract void readHeader(Segment memorySegment);

    public boolean isLoaded() {
        return loaded;
    }

    public synchronized void load() {
        String path = getPath();
        File file = new File(path);

        if(!file.exists()) {
            throw new IllegalStateException("Binary file " + path + " does not exist");
        }

        try(Arena arena = Arena.ofConfined()) {
            Segment segment = FileLoader.readSegment(arena, file, getHeaderSize());
            readHeader(segment);
            setBackingSegment(FileLoader.loadMapped(file, 0, getHeaderSize(), false));
        }
    }



    public void allocate() {

    }

    public abstract void setBackingSegment(Segment segment);

    public abstract void writeHeader(SegmentTable segment);

    public long getGameVersionNumber() {
        return gameVersion;
    }

    public long getFileID() {
        return fileID;
    }
}
