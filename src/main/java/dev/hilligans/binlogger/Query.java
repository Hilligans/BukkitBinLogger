package dev.hilligans.binlogger;

import java.util.UUID;

public class Query {

    public int properties;

    public short action;
    public short user;
    public UUID player;

    public int x;
    public int y;
    public int z;

    public long startTime;
    public long endTime;
    public long data;
    public long dataMask;

    public int minX;
    public int minY;
    public int minZ;

    public int maxX;
    public int maxY;
    public int maxZ;


    public int perRegionQuery = 50;

    public Query withAction(Action action) {
        this.action = action.getID();
        setProperty(ACTION);
        return this;
    }

    public Query withUser(short user) {
        this.user = user;
        setProperty(USER);
        return this;
    }

    public Query withPlayer(UUID player) {
        this.player = player;
        setProperty(PLAYER);
        return this;
    }

    public Query withPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        setProperty(POSITION);
        return this;
    }

    public Query withTime(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        setProperty(TIME);
        return this;
    }

    public Query withData(long data, long dataMask) {
        this.data = data;
        this.dataMask = dataMask;
        return this;
    }

    public Query withRadius(int radius) {
        if(!getProperty(POSITION)) {
            throw new RuntimeException("Query position must be set first to set a radius");
        }
        this.minX = this.x - radius;
        this.minY = this.y - radius;
        this.minZ = this.z - radius;

        this.maxX = this.x + radius;
        this.maxY = this.y + radius;
        this.maxZ = this.z + radius;

        setProperty(BOUNDING_BOX);
        return this;
    }

    public boolean getProperty(long property) {
        return (this.properties & property) != 0;
    }

    public void setProperty(long property) {
        this.properties |= property;
    }



    public static final int ACTION = 1;
    public static final int USER = 1 << 1;
    public static final int POSITION = 1 << 2;
    public static final int TIME = 1 << 3;
    public static final int DATA = 1 << 4;
    public static final int BOUNDING_BOX = 1 << 5;
    public static final int PLAYER = 1 << 6;

}

