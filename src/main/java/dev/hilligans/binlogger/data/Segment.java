package dev.hilligans.binlogger.data;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Segment implements SegmentTable, AutoCloseable {

    public MemorySegment backingSegment;
    public FileChannel backingChannel;
    public long position;

    public Segment(MemorySegment backingSegment) {
        this.backingSegment = backingSegment;
    }

    public Segment(MemorySegment backingSegment, FileChannel backingChannel) {
        this.backingSegment = backingSegment;
        this.backingChannel = backingChannel;
    }

    @Override
    public void writeByte(byte val) {
        backingSegment.set(ValueLayout.JAVA_BYTE, position, val);
        position++;
    }

    @Override
    public void writeShort(short val) {
        backingSegment.set(ValueLayout.JAVA_SHORT, position, val);
        position += 2;
    }

    @Override
    public void writeChar(char val) {
        backingSegment.set(ValueLayout.JAVA_CHAR, position, val);
        position += 2;
    }

    @Override
    public void writeInt(int val) {
        backingSegment.set(ValueLayout.JAVA_INT, position, val);
        position += 4;
    }

    @Override
    public void writeLong(long val) {
        backingSegment.set(ValueLayout.JAVA_LONG, position, val);
        position += 8;
    }

    @Override
    public void writeFloat(float val) {
        backingSegment.set(ValueLayout.JAVA_FLOAT, position, val);
        position += 4;
    }

    @Override
    public void writeDouble(double val) {
        backingSegment.set(ValueLayout.JAVA_DOUBLE, position, val);
        position += 8;
    }

    @Override
    public void writeString(String val) {
        backingSegment.setString(position, val, StandardCharsets.UTF_8);
        position += val.length();
    }

    @Override
    public byte readByte() {
        position -= 1;
        return backingSegment.get(ValueLayout.JAVA_BYTE, position+1);
    }

    @Override
    public short readShort() {
        position -= 2;
        return backingSegment.get(ValueLayout.JAVA_SHORT, position+2);
    }

    @Override
    public char readChar() {
        position -= 2;
        return backingSegment.get(ValueLayout.JAVA_CHAR, position+2);
    }

    @Override
    public int readInt() {
        position -= 4;
        return backingSegment.get(ValueLayout.JAVA_INT, position+4);
    }

    @Override
    public long readLong() {
        position -= 8;
        return backingSegment.get(ValueLayout.JAVA_LONG, position+8);
    }

    @Override
    public float readFloat() {
        position -= 4;
        return backingSegment.get(ValueLayout.JAVA_FLOAT, position+4);
    }

    @Override
    public double readDouble() {
        position -= 8;
        return backingSegment.get(ValueLayout.JAVA_DOUBLE, position+8);
    }

    @Override
    public String readString() {
        String string = backingSegment.getString(position);
        position -= string.length();
        return string;
    }

    @Override
    public void close() throws IOException {
        if(backingChannel != null) {
            backingChannel.close();
        }
    }
}
