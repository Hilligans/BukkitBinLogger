package dev.hilligans.binlogger;

import java.time.Instant;
import java.time.ZoneId;

public class Save {

    public static String getSaveDirectory(String worldName, int x, int z, String dataType) {
        return "binlogger/worlds/" + worldName + "/" + x + "_" + z + "/" + dataType;
    }

    public static String getCurrentDate() {
        return Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().toString();
    }
}
