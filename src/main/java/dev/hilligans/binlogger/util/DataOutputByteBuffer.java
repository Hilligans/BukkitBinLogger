package dev.hilligans.binlogger.util;

import org.apache.commons.codec.Charsets;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DataOutputByteBuffer implements DataOutput {

    ByteBuffer buffer;
    public int position;

    public DataOutputByteBuffer(ByteBuffer buffer, int position) {
        this.buffer = buffer;
        this.position = position;
    }

    @Override
    public void write(int b) {
        buffer.put(position++, (byte)b);
    }

    @Override
    public void write(@NotNull byte[] b) {
        buffer.put(position, b);
        position += b.length;
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) {
        buffer.put(position, b, off, len);
        position += b.length;
    }

    @Override
    public void writeBoolean(boolean v) {
        buffer.put(position++, (byte)(v ? 1 : 0));
    }

    @Override
    public void writeByte(int v) {
        write(v);
    }

    @Override
    public void writeShort(int v) {
        buffer.putShort(position, (short)v);
        position += 2;
    }

    @Override
    public void writeChar(int v) {
        buffer.putChar(position, (char)v);
        position += 2;
    }

    @Override
    public void writeInt(int v) {
        buffer.putInt(position, v);
        position += 4;
    }

    @Override
    public void writeLong(long v) {
        buffer.putLong(position, v);
        position += 8;
    }

    @Override
    public void writeFloat(float v) {
        buffer.putFloat(position, v);
        position += 4;
    }

    @Override
    public void writeDouble(double v) {
        buffer.putDouble(position, v);
        position += 8;
    }

    @Override
    public void writeBytes(@NotNull String s) {
        write(s.getBytes(Charsets.UTF_8));
    }

    @Override
    public void writeChars(@NotNull String s) {
        write(s.getBytes());
    }

    @Override
    public void writeUTF(@NotNull String s) throws IOException {
        final int strlen = s.length();
        int utflen = strlen; // optimized for ASCII

        for (int i = 0; i < strlen; i++) {
            int c = s.charAt(i);
            if (c >= 0x80 || c == 0)
                utflen += (c >= 0x800) ? 2 : 1;
        }

        if (utflen > 65535 || /* overflow */ utflen < strlen)
            throw new UTFDataFormatException();

        int count = 0;
        buffer.put(position + count++, (byte) ((utflen >>> 8) & 0xFF));
        buffer.put(position + count++, (byte) ((utflen >>> 0) & 0xFF));

        int i;
        for (i = 0; i < strlen; i++) { // optimized for initial run of ASCII
            int c = s.charAt(i);
            if (c >= 0x80 || c == 0) break;
            buffer.put(position + count++, (byte) c);
        }

        for (; i < strlen; i++) {
            int c = s.charAt(i);
            if (c < 0x80 && c != 0) {
                buffer.put(position + count++, (byte) c);
            } else if (c >= 0x800) {
                buffer.put(position + count++, (byte) (0xE0 | ((c >> 12) & 0x0F)));
                buffer.put(position + count++, (byte) (0x80 | ((c >> 6) & 0x3F)));
                buffer.put(position + count++, (byte) (0x80 | ((c >> 0) & 0x3F)));
            } else {
                buffer.put(position + count++, (byte) (0xC0 | ((c >>  6) & 0x1F)));
                buffer.put(position + count++, (byte) (0x80 | ((c >>  0) & 0x3F)));
            }
        }
    }
}
