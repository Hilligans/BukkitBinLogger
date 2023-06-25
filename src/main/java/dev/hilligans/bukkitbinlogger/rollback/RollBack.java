package dev.hilligans.bukkitbinlogger.rollback;

import dev.hilligans.binlogger.Action;
import dev.hilligans.binlogger.Database;
import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;

import java.util.ArrayList;

public class RollBack {

    public World world;
    public ArrayList<Database.DatabaseEntry> entries;
    public Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<SubChunk>> sections = new Long2ObjectOpenHashMap<>();
    public ArrayList<SubChunk> newSections;

    public RollBack(World world, ArrayList<Database.DatabaseEntry> entries) {
        this.world = world;
        this.entries = entries;
    }

    public void build() {
        newSections = new ArrayList<>();
        sections.clear();
        for(Database.DatabaseEntry databaseEntry : entries) {
            int x = databaseEntry.getX();
            int y = databaseEntry.getY();
            int z = databaseEntry.getZ();
            SubChunk subChunk = get(x, y, z);
            Action action = BukkitBinLogger.database.actionRegistry.getAction(databaseEntry.getAction());
            IBlockData blockData = action.getRollback(databaseEntry, IBlockData.class);
            if(blockData == null) {
                continue;
            }
            subChunk.setBlockState(x, y, z, blockData);
        }
    }

    public void apply() {
        CraftWorld world1 = world.getWorld();
        for(Database.DatabaseEntry databaseEntry : entries) {
            int x = databaseEntry.getX();
            int y = databaseEntry.getY();
            int z = databaseEntry.getZ();
            Action action = BukkitBinLogger.database.actionRegistry.getAction(databaseEntry.getAction());
            IBlockData blockData = action.getRollback(databaseEntry, IBlockData.class);
            if(blockData == null) {
                continue;
            }
            //System.out.println("SetBlock " + ((BlockData)blockData).getAsString());

            world1.setBlockData(x, y, z, CraftBlockData.fromData(blockData));
            //world.a(new BlockPosition(x, y, z), blockData);
            //world1.setBlockData(x, y, z, (BlockData) blockData);
            //ChunkSection section = ((CraftChunk) world.getWorld().getChunkAt(x >> 4, z >> 4)).getHandle(ChunkStatus.o).d()[y >> 4];
            //section.a(x & 0xF, y & 0xF, z & 0xF, blockData);
        }
        //RollbackPreview.sendCleared(this);
    }

    public ArrayList<SubChunk> getNewChunkSections() {
        return newSections;
    }


    public SubChunk get(long x, int y, long z) {
        Short2ObjectOpenHashMap<SubChunk> sections1 = sections.get((x << 32L) | z);
        if(sections1 == null) {
            sections1 = new Short2ObjectOpenHashMap<>();
        }

        ChunkSection section = ((CraftChunk) world.getWorld().getChunkAt((int) x >> 4, (int) z >> 4)).getHandle(ChunkStatus.o).d()[y >> 4];
        return sections1.computeIfAbsent((short)y, (int a) -> {
            SubChunk subChunk = new SubChunk(new ChunkSection(section.g() >> 4, section.i().d(), ((DataPaletteBlock<Holder<BiomeBase>>) section.j()).d()), (int) (x >> 4), (int) (z >> 4), world, y >> 4);
            newSections.add(subChunk);
            return subChunk;
        });
    }

    public static class SubChunk {

        public ShortArraySet positions = new ShortArraySet();
        public ChunkSection chunkSection;
        public SectionPosition pos;
        public int x;
        public int z;
        public int y;
        public World world;

        public SubChunk(ChunkSection section, int x, int z, World world, int y) {
            this.chunkSection = section;
            this.x = x;
            this.z = z;
            this.y = y;
            //TODO fix this
            this.pos = SectionPosition.a(x, (chunkSection.g() >> 4) + 4, z);
            this.world = world;
        }

        public void setBlockState(int x, int y, int z, IBlockData block) {
            chunkSection.a(x & 0xF, y & 0xF, z & 0xF, block);
            positions.add((short) ((x & 0xF) << 8 | (z & 0xF) << 4 | (y & 0xF)));
        }

        @Override
        public String toString() {
            return "SubChunk{" +
                    "positions=" + positions +
                    ", pos=" + pos +
                    ", x=" + x +
                    ", z=" + z +
                    ", y=" + y +
                    '}';
        }
    }
}
