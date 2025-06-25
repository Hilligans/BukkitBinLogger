package dev.hilligans.binlogger.data;

public interface SegmentTable {

    void writeByte(byte val);
    void writeShort(short val);
    void writeChar(char val);
    void writeInt(int val);
    void writeLong(long val);
    void writeFloat(float val);
    void writeDouble(double val);
    void writeString(String val);

    byte readByte();
    short readShort();
    char readChar();
    int readInt();
    long readLong();
    float readFloat();
    double readDouble();
    String readString();

}
