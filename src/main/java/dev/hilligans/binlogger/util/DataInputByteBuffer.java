package dev.hilligans.binlogger.util;

import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

public class DataInputByteBuffer implements DataInput {

    ByteBuffer buffer;
    public int position;

    public DataInputByteBuffer(ByteBuffer buffer, int position) {
        this.buffer = buffer;
        this.position = position;

      //  new ByteBufferBack()
    }

    @Override
    public void readFully(@NotNull byte[] b) throws IOException {
        //todo implement
    }

    @Override
    public void readFully(@NotNull byte[] b, int off, int len) throws IOException {
        //todo implement
    }

    @Override
    public int skipBytes(int n) throws IOException {
        position+=n;
        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readByte() == 1;
    }

    @Override
    public byte readByte() throws IOException {
        return buffer.get(position++);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(readByte());
    }

    @Override
    public short readShort() throws IOException {
        int pos = position;
        position += 2;
        return buffer.getShort(pos);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(readShort());
    }

    @Override
    public char readChar() throws IOException {
        int pos = position;
        position += 2;
        return buffer.getChar(pos);
    }

    @Override
    public int readInt() throws IOException {
        int pos = position;
        position += 4;
        return buffer.getInt(pos);
    }

    @Override
    public long readLong() throws IOException {
        int pos = position;
        position += 8;
        return buffer.getLong(pos);
    }

    @Override
    public float readFloat() throws IOException {
        int pos = position;
        position += 4;
        return buffer.getFloat(pos);
    }

    @Override
    public double readDouble() throws IOException {
        int pos = position;
        position += 8;
        return buffer.getDouble(pos);
    }

    @Override
    public String readLine() throws IOException {
        //todo implement
        return null;
    }

    public String readUTF() throws IOException {
        int utflen = readUnsignedShort();
        char[] chararr = new char[utflen];
        int c, char2, char3;
        int count = 0;
        int chararr_count=0;

        while (count < utflen) {
            c = (int) buffer.get(count + position) & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
        }

        while (count < utflen) {
            c = (int) buffer.get(count + position) & 0xff;
            switch (c >> 4) {
                case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++]=(char)c;
                }
                case 12, 13 -> {
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) buffer.get(count + position - 1);
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                }
                case 14 -> {
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) buffer.get(count + position - 2);
                    char3 = (int) buffer.get(count + position - 1);
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6)  |
                            ((char3 & 0x3F) << 0));
                }
                default ->
                    /* 10xx xxxx,  1111 xxxx */
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
