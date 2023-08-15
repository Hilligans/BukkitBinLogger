package dev.hilligans.binlogger.util;

import java.text.DecimalFormat;

public class TimeUtil {

    public static String getTimeSince(long resultTime, long currentTime) {
        StringBuilder message = new StringBuilder();
        double timeSince = currentTime - (resultTime + 0.00);
        if (timeSince < 0.00) {
            timeSince = 0.00;
        }

        // minutes
        timeSince = timeSince / 60;
        if (timeSince < 60.0) {
            message.append(new DecimalFormat("0.00").format(timeSince)).append("/m");
        }

        // hours
        if (message.length() == 0) {
            timeSince = timeSince / 60;
            if (timeSince < 24.0) {
                message.append(new DecimalFormat("0.00").format(timeSince)).append("/h");
            }
        }

        // days
        if (message.length() == 0) {
            timeSince = timeSince / 24;
            message.append(new DecimalFormat("0.00").format(timeSince)).append("/d");
        }
        return message.toString();
    }


}
