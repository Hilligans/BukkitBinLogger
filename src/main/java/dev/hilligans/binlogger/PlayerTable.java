package dev.hilligans.binlogger;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerTable {

    public ArrayList<UUID> players;
    public Object2ShortOpenHashMap<UUID> map = new Object2ShortOpenHashMap<>();
    boolean fullyInMemory;
    boolean optimizedTable;

    public PlayerTable() {
        players = new ArrayList<>();
        this.fullyInMemory = true;
    }

    public PlayerTable(ArrayList<UUID> players, boolean optimizedTable) {
        this.players = players;
        this.optimizedTable = optimizedTable;
        for(short x = 0; x < players.size(); x++) {
            map.put(players.get(x), x);
        }
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
        buffer.getLong();
        buffer.getLong();
        int size = buffer.getInt();
        buffer.getInt();
        buffer.getLong();

        players = new ArrayList<>(size);

        for(short x = 0; x < size; x++) {
            UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
            players.set(x, uuid);
            map.put(uuid, x);
        }
    }

    public void save(ByteBuffer buffer) {
        buffer.put("Binary Log Table".getBytes(StandardCharsets.US_ASCII));

        buffer.putInt(players.size());
        buffer.putInt(0);
        buffer.putLong(0);

        for(UUID player : players) {
            buffer.putLong(player.getMostSignificantBits());
            buffer.putLong(player.getLeastSignificantBits());
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
