package dev.hilligans.binlogger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.time.ZoneId;

public class Save {

    public static String getSaveDirectory(String worldName, int x, int z, String dataType) {
        return "binlogger/worlds/" + worldName + "/" + x + "_" + z + "/" + dataType;
    }

    public static String getCurrentDate() {
        return Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().toString();
    }

    public static void saveString(String path, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(data);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String readString(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream stream = Save.class.getResourceAsStream(path);
        if(stream == null) {
            throw new RuntimeException("Failed to load file " + path);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.lines().forEach(string -> stringBuilder.append(string).append("\n"));
        return stringBuilder.toString();
    }
}
