package dev.hilligans.binlogger;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerTable {

    public ArrayList<UUID> players = new ArrayList<>();
    public Object2ShortOpenHashMap<UUID> map = new Object2ShortOpenHashMap<>();
    boolean fullyInMemory;

    public PlayerTable() {
        this.fullyInMemory = true;
    }

    public PlayerTable(String path, boolean writing) {
        try {
            try(RandomAccessFile aFile = new RandomAccessFile(path, writing ? "rw" : "r")) {
                if (writing) {

                } else {
                    this.fullyInMemory = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(ByteBuffer buffer) {

    }

    public void save(ByteBuffer buffer) {
        buffer.put("Binary Log Table".getBytes(StandardCharsets.US_ASCII));

        buffer.putInt(players.size());
        buffer.putInt(0);
        buffer.putLong(0);

        for(UUID player : players) {
            buffer.putLong(player.getLeastSignificantBits());
            buffer.putLong(player.getMostSignificantBits());
        }
    }



    public UUID getUUID(short player) {
        return players.get(player);
    }

    public short getPlayer(UUID uuid) {
        if(map.containsKey(uuid)) {
            return map.getShort(uuid);
        } else {
            short index = (short) players.size();
            players.add(uuid);
            map.put(uuid, index);
            return index;
        }
    }
}
