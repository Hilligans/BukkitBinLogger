package dev.hilligans.binlogger;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerTable {

    public ArrayList<UUID> players;
    public Object2ShortOpenHashMap<UUID> map = new Object2ShortOpenHashMap<>();
    boolean fullyInMemory;
    boolean optimizedTable;

    ByteBuffer headBuffer;

    public PlayerTable(Region region) {
        players = new ArrayList<>();
        File f = new File(Save.getSaveDirectory(region.worldName, region.x, region.z, "players-") + region.date + "-" + region.count + ".dat");
        try {
            RandomAccessFile aFile;
            if (f.exists()) {
                aFile = new RandomAccessFile(f, "rw");
                headBuffer = aFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, aFile.length());
                load(headBuffer);
            } else {
                aFile = new RandomAccessFile(f, "rw");
                aFile.setLength((long) 256 * 16 + 32);
                headBuffer = aFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, aFile.length());

                headBuffer.put(0, "Binary Log Table".getBytes(StandardCharsets.US_ASCII));
                headBuffer.putInt(16, players.size());
                headBuffer.putInt(20, 0);
                headBuffer.putLong(24, 0);
            }
            aFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.fullyInMemory = true;
    }

    public PlayerTable(ArrayList<UUID> players, boolean optimizedTable) {
        this.players = players;
        this.optimizedTable = optimizedTable;
        for(short x = 0; x < players.size(); x++) {
            map.put(players.get(x), x);
        }
    }

    public void load(ByteBuffer buffer) {
        buffer.getLong();
        buffer.getLong();
        int size = buffer.getInt();
        buffer.getInt();
        buffer.getLong();

        players = new ArrayList<>(size);
        System.out.println("Loading size " + size);
        for(short x = 0; x < size; x++) {
            UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
            players.add(uuid);
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
            headBuffer.putLong(index * 16 + 32, uuid.getMostSignificantBits());
            headBuffer.putLong(index * 16 + 40, uuid.getLeastSignificantBits());
            headBuffer.putInt(16, index + 1);
            return index;
        }
    }
}
