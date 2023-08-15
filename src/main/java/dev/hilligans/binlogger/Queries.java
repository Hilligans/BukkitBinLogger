package dev.hilligans.binlogger;

import java.nio.ByteBuffer;

public class Queries {

    public static int queryForAction(Region region, short actionID, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getShort(i * 16) & 0x7FFF) == actionID) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForOwner(Region region, short owner, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(byteBuffer.getShort(i * 16 + 2) == owner) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForActionOwner(Region region, short action, short owner, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int val = ((action << 16) | owner);
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getInt(i * 16) & 0x7FFFFFFF) == val) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForPosition(Region region, int x, int y, int z, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(byteBuffer.getInt(i * 16 + 4) == pos) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }


    public static int queryForActionPosition(Region region, short action, int x, int y, int z, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)action & 0xFFFF) << 48) | pos;
        long mask = (~(0xFFL << 32)) & (~(1L << 63));
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForUserPosition(Region region, short user, int x, int y, int z, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)user & 0xFFFF) << 32) | pos;
        long mask = ~(0xFFL << 48);
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForActionUserPosition(Region region, short action, short user, int x, int y, int z, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = ((((long)action) & 0xFFFF) << 48) | (((long)user & 0xFFFF) << 32) | pos;
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(((byteBuffer.getLong(i * 16)) & (~(1L << 63))) == val) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if(offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static long calculateTime(short ticks, long time) {
        return time + ticks * 1000 / 20;
    }

    public static int calculateTickMaxTimestamp(long time, long endTime) {
        long delta = endTime - time;
        return (int) (delta / 1000 * 20);
    }

    public static int getStartPosition(Region region, long start) {
        int x = 0;
        int bufferPointer = region.getBufferPointer();
        ByteBuffer byteBuffer = region.getBufferForReading();
        int timeHeaderOffset = region.getTimeHeaderOffset();
        while (bufferPointer > x) {
            if ((byteBuffer.getLong(x * 16) & 0xFFFF000000000000L) == (1L << 48L)) {
                if (byteBuffer.getLong(x * 16 + 8) > start) {
                    x -= timeHeaderOffset;
                    if (x < 0) {
                        x = 0;
                    }
                    break;
                }
            }
            x += timeHeaderOffset;
        }
        return x;
    }

    public static int queryForTime(Region region, long startTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, startTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if (i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if (byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if (offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForActionTime(Region region, short action, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getShort(i * 16) & 0x7FFF) == action) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForOwnerTime(Region region, short owner, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if(byteBuffer.getShort(i * 16 + 2) == owner) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionOwnerTime(Region region, short action, short owner, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int val = ((action << 16) | owner);

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getInt(i * 16) & 0x7FFF) == val) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForPositionTime(Region region, int x, int y, int z, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if(byteBuffer.getInt(i * 16 + 4) == pos) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }


    public static int queryForActionPositionTime(Region region, short action, int x, int y, int z, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)action & 0xFFFF) << 48) | pos;
        long mask = ~(0xFFL << 32) & (~(1L << 63));

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForUserPositionTime(Region region, short user, int x, int y, int z, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)user & 0xFFFF) << 32) | pos;
        long mask = ~(0xFFL << 48);

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionUserPositionTime(Region region, short action, short user, int x, int y, int z, long starTime, long endTime, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = ((((long)action) & 0xFFFF) << 48) | (((long)user & 0xFFFF) << 32) | pos;

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        long mask = (~(1L << 63));
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if(((byteBuffer.getLong(i * 16)) & mask) == val) {
                if(byteBuffer.getShort(i * 16 + 8) < maxTickTime) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }



//    public static final long DATA_MASK = 0xFFFFFFFFFFFFL;

    public static int queryForData(Region region, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if (i % timeHeaderOffset == 0) {
                continue;
            }
            if ((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if (offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }


    public static int queryForActionData(Region region, short actionID, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getShort(i * 16) & 0x7FFF) == actionID) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForOwnerData(Region region, short owner, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(byteBuffer.getShort(i * 16 + 2) == owner) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionOwnerData(Region region, short action, short owner, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int val = ((action << 16) | owner);
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getInt(i * 16) & 0x7FFFFFFF) == val) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForPositionData(Region region, int x, int y, int z, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(byteBuffer.getInt(i * 16 + 4) == pos) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }


    public static int queryForActionPositionData(Region region, short action, int x, int y, int z, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)action & 0xFFFF) << 48) | pos;
        long mask = ~(0xFFL << 32) & ~(1L << 63);
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForUserPositionData(Region region, short user, int x, int y, int z, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)user & 0xFFFF) << 32) | pos;
        long mask = ~(0xFFL << 48);
        int offset = 0;
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionUserPositionData(Region region, short action, short user, int x, int y, int z, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = ((((long)action) & 0xFFFF) << 48) | (((long)user & 0xFFFF) << 32) | pos;
        int offset = 0;
        long mask = ~(1L << 63);
        for(int i = 0; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                continue;
            }
            if(((byteBuffer.getLong(i * 16)) & mask) == val) {
                if((byteBuffer.getLong(i * 16 + 8) & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForTimeData(Region region, long startTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, startTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if (i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            long dat = byteBuffer.getLong(i * 16 + 8);
            if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                data[offset + dataOffset] = i;
                offset++;
                limit--;
                if (offset + dataOffset == data.length || limit == 0) {
                    return offset;
                }
            }
        }
        return offset;
    }

    public static int queryForActionTimeData(Region region, short action, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getShort(i * 16) & 0x7FFF) == action) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForOwnerTimeData(Region region, short owner, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if(byteBuffer.getShort(i * 16 + 2) == owner) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionOwnerTimeData(Region region, short action, short owner, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int val = ((action << 16) | owner);

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getInt(i * 16) & 0x7FFFFFFF) == val) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForPositionTimeData(Region region, int x, int y, int z, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if(byteBuffer.getInt(i * 16 + 4) == pos) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }


    public static int queryForActionPositionTimeData(Region region, short action, int x, int y, int z, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)action & 0xFFFF) << 48) | pos;
        long mask = ~(0xFFL << 32) & ~(1L << 63);

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForUserPositionTimeData(Region region, short user, int x, int y, int z, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = (((long)user & 0xFFFF) << 32) | pos;
        long mask = ~(0xFFL << 48);

        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }

    public static int queryForActionUserPositionTimeData(Region region, short action, short user, int x, int y, int z, long starTime, long endTime, long dataIn, final long dataMask, int[] data, int dataOffset, int limit) {
        ByteBuffer byteBuffer = region.getBufferForReading();
        int bufferPointer = region.getBufferPointer();
        int timeHeaderOffset = region.getTimeHeaderOffset();

        int pos = (int) (((x & 0x7FFL) << 21) | ((z & 0x7FFL) << 10) | (y & 0x3FFL));
        long val = ((((long)action) & 0xFFFF) << 48) | (((long)user & 0xFFFF) << 32) | pos;
        long mask = ~(1L << 63);
        int i = getStartPosition(region, starTime);
        int maxTickTime = 0;
        int offset = 0;
        for(; i < bufferPointer; i++) {
            if(i % timeHeaderOffset == 0) {
                maxTickTime = calculateTickMaxTimestamp(byteBuffer.getLong(i * 16 + 8), endTime);
                continue;
            }
            if((byteBuffer.getLong(i * 16) & mask) == val) {
                long dat = byteBuffer.getLong(i * 16 + 8);
                if(dat >> 48L < maxTickTime && (dat & dataMask) == dataIn) {
                    data[offset + dataOffset] = i;
                    offset++;
                    limit--;
                    if (offset + dataOffset == data.length || limit == 0) {
                        return offset;
                    }
                }
            }
        }
        return offset;
    }
}
