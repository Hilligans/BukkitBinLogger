package dev.hilligans.binlogger.util;

import dev.hilligans.binlogger.data.Segment;

import java.io.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;

import static java.nio.file.StandardOpenOption.*;

public class FileLoader {

    public static String readString(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream stream = FileLoader.class.getResourceAsStream(path);
        if(stream == null) {
            System.out.println("Cant read file: " + path);
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.lines().forEach(string -> stringBuilder.append(string).append("\n"));
        return stringBuilder.toString();
    }

    public static MemorySegment readHeader(Arena arena, File file, long size) {
        if(!file.exists()) {
            throw new RuntimeException("File not found: " + file);
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long totalSize = randomAccessFile.getChannel().size();
            if(size > totalSize) {
                throw new RuntimeException("File " + file + " does not contain a header, potentially corrupted?");
            }

            MemorySegment s = arena.allocate(size);
            randomAccessFile.getChannel().read(s.asByteBuffer());
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Segment readSegment(Arena arena, File file, long size) {
        return new Segment(readHeader(arena,file,size));
    }

    public static Segment loadMapped(File file, long size, long offset, boolean writing) {
        try {
            FileChannel channel = FileChannel.open(file.toPath(), writing ? new OpenOption[]{READ, WRITE} : new OpenOption[]{READ});
            return new Segment(channel.map(writing ? FileChannel.MapMode.READ_WRITE : FileChannel.MapMode.READ_ONLY, offset, size, Arena.ofAuto()), channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Segment newFile(File file, long size) {
        try {
            FileChannel channel = FileChannel.open(file.toPath(), READ, WRITE, CREATE_NEW);
            MemorySegment segment = channel.map(FileChannel.MapMode.READ_WRITE, 0, size, Arena.ofAuto());

            return new Segment(segment, channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
