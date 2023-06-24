package dev.hilligans.binlogger;

import dev.hilligans.bukkitbinlogger.rollback.RollBack;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.World;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;

import static dev.hilligans.bukkitbinlogger.BukkitBinLogger.database;

public abstract class Database {

    public final Long2ObjectOpenHashMap<Region> regionMap = new Long2ObjectOpenHashMap<>();
    public final Object2ObjectOpenHashMap<String, Long2ObjectOpenHashMap<Region>> map = new Object2ObjectOpenHashMap<>();
    public final ActionRegistry actionRegistry = new ActionRegistry();

    public ArrayList<DatabaseEntry> parseQuery(String[] query, int limit, String world) {

        int x = Integer.parseInt(query[0]);
        int y = Integer.parseInt(query[1]);
        int z = Integer.parseInt(query[2]);

        Region region = getOrCreateRegion(x >> 11, z >> 11, world);
        int[] data = new int[limit];
        int count = region.query(null, null, x, y, z, null, null, data, 0);

        ArrayList<DatabaseEntry> entries = new ArrayList<>();
        for(int a = 0; a < count; a++) {
            entries.add(region.getEntry(data[a]));
        }

        return entries;
    }

    public String parseQuery(String[] query, String world) {
        ArrayList<DatabaseEntry> entries = parseQuery(query, 100, world);

        int x = Integer.parseInt(query[0]);
        int y = Integer.parseInt(query[1]);
        int z = Integer.parseInt(query[2]);

        long time = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        builder.append("showing ").append(entries.size()).append(" results for ").append("X:").append(x).append(" Y:").append(y).append(" Z:").append(z).append("\n");
        for(int a = 0; a < Math.min(5, entries.size()); a++) {
            builder.append(entries.get(a).getFormattedString(this, time)).append("\n");
        }
        return builder.toString();
    }

    public RollBack getRollback(String[] query, World world) {
        ArrayList<DatabaseEntry> entries = parseQuery(query, 1, world.getWorld().getName());
        return new RollBack(world, entries);
    }

    public void logAction(Action action, User user, int x, int y, int z, String world, long time, long data) {
        Region region = getOrCreateRegion(x >> 11, z >> 11, world);
        region.write(action, user.getID(), x, y, z, time, data);
    }

    public void logAction(Action action, UUID player, int x, int y, int z, String world, long time, long data) {
        Region region = getOrCreateRegion(x >> 11, z >> 11, world);
        short pid = region.getPlayerTable().getPlayer(player);
        region.write(action, pid, x, y, z, time, data);
    }

    public @NotNull Region getOrCreateRegion(int x, int z, String world) {
        synchronized (regionMap) {
            long key = ((long) x << 32L) | ((long) z) & 0xFFFFFFFFL ;
            Region region = regionMap.get(key);
            if(region == null) {
                region = new Region(x, z, world, getProtocolVersion(), getActionChecksum());
            }
            regionMap.put(key, region);
            return region;
        }
    }

    public void putRegion(int x, int z, String world, Region region) {
        long key = ((long) x << 32L) | ((long) z) & 0xFFFFFFFFL;
        regionMap.put(key, region);
    }

    public void loadActiveTablesToMemory(String world) {
        String currentDate = Save.getCurrentDate();
        File f = new File("binlogger/worlds/" + world + "/");
        if(f.exists()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        System.out.println(file.getPath());
                        File data = new File(file.getPath() + "/" + currentDate + "_0.dat");
                        if(data.exists()) {
                            try {
                                Region region = Region.createMappedRegion(data);
                                if(region == null) {
                                    throw new RuntimeException("Failed to load log table");
                                }
                                putRegion(region.x, region.z, region.worldName, region);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                        }
                    }
                }
            }
        }
    }

    public long getActionChecksum() {
        return actionRegistry.getChecksum();
    }

    public abstract void registerAllContent();
    public abstract String getBlockName(short blockID);
    public abstract String getItemName(short itemID);
    public abstract String getPlayerName(UUID uuid);
    public abstract String getEntityName(short entityID);
    public abstract int getProtocolVersion();

    public static class DatabaseEntry {

        Region region;

        long timestamp;

        long data1, data2;

        public DatabaseEntry(Region region, long timestamp, long data1, long data2) {
            this.region = region;
            this.timestamp = timestamp;
            this.data1 = data1;
            this.data2 = data2;
        }

        public int getAction() {
            return (short) (data1 >>> 48L);
        }

        public short getOwner() {
            return (short) ((data1 >> 32L) & 0xFFFF);
        }

        public int getPosition() {
            return (int) (data1 & 0xFFFFFFFFL);
        }

        public short getTime() {
            return (short) (data2 >>> 48L);
        }

        public long getData() {
            return (data2 & 0xFFFFFFFFFFFFL);
        }

        public int getX() {
            return (getPosition() >>> 21) | (region.x << 11);
        }

        public int getY() {
            return getPosition() & 0b1111111111;
        }

        public int getZ() {
            return (getPosition() >> 10 & 0b11111111111) | (region.z << 11);
        }

        public String getFormattedString(Database database, long time) {

            StringBuilder builder = new StringBuilder();
            builder.append(DurationFormatUtils.formatDurationWords(time - Region.calculateTime(getTime(), timestamp), true, true));

            Action action = database.actionRegistry.getAction(getAction());
            String user;
            short u = getOwner();
            if((u & (short)(1 << 15)) == 0) {
                user = database.getPlayerName(region.getPlayerTable().getUUID(u));
            } else {
                if((u & (short)(1 << 14)) == 0) {
                    u = (short) (u ^ (0b10 << 14));
                    user = database.getEntityName(u);
                } else {
                    u = (short) (u ^ (0b11 << 14));
                    user = "#" + User.values()[u].name();
                }
            }

            builder.append(" ago ").append(action.icon).append(" ").append(user).append(" ").append(action.word);

            BiFunction<Long, Database, String> biFunction = action.parser;
            if(biFunction != null) {
                builder.append(" ").append(biFunction.apply(getData(), database).toLowerCase());
            } else {
                builder.append(" no parser found");
            }
            return builder.toString();
        }

        public BlockData getBlockState(Database database) {
            Action action = database.actionRegistry.getAction(getAction());
            if(action.parser == Database.BLOCK_PARSER) {
                Material material = CraftMagicNumbers.getMaterial(net.minecraft.world.level.block.Block.a((int) (getData() >> (4 * 8))).b());
                return material.createBlockData();
            }
            return null;
        }
    }

    public static class BlockDatabaseEntry {

    }

    public static final BiFunction<Long, Database, String> BLOCK_PARSER = (aLong, database) -> database.getBlockName((short) (aLong >>> 32));
}
