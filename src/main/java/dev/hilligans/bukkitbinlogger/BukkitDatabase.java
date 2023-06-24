package dev.hilligans.bukkitbinlogger;

import dev.hilligans.binlogger.Action;
import dev.hilligans.binlogger.Database;
import dev.hilligans.binlogger.Region;
import dev.hilligans.binlogger.User;
import dev.hilligans.bukkitbinlogger.events.ActionLoadEvent;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitDatabase extends Database {

    public void logAction(Action action, Entity entity, int x, int y, int z, String world, long time, long data) {
        Region region = getOrCreateRegion(x >> 11, z >> 11, world);
        short id = (short) (MAP.getShort(entity.getType()) | ((short)(1 << 15)));
        region.write(action, id, x, y, z, time, data);
    }

    public void logBlockBreak(Player player, Block block) {
        long id = getBlockID(block);
        logAction(Action.BREAK, player.getUniqueId(), block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), id << (4 * 8));
    }

    public void logBlockBreak(User user, Block block) {
        long id = getBlockID(block);
        logAction(Action.BREAK, user, block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), id << (4 * 8));
    }

    public void logBlockPlace(Player player, Block block) {
        long id = getBlockID(block);
        logAction(Action.PLACE, player.getUniqueId(), block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), id << (4 * 8));
    }

    public void logBlockReplace(User user, Block oldBlock, Block newBlock) {
        long oldID = getBlockID(oldBlock);
        long newID = getBlockID(newBlock);
        logAction(Action.PLACE, user, oldBlock.getX(), oldBlock.getY(), oldBlock.getZ(), oldBlock.getWorld().getName(), System.currentTimeMillis(), (oldID << (4 * 8)) | (newID << (2 * 8)) );
    }

    public void logBlockAction(Action action, User user, Block block, long data) {
        logAction(action, user, block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), data);
    }

    public void logBlockAction(Action action, UUID user, Block block, long data) {
        logAction(action, user, block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), data);
    }

    public void logBlockAction(Action action, Entity entity, Block block, long data) {
        logAction(action, entity, block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), System.currentTimeMillis(), data);
    }

    public <T> void logBlockAction(Action action, T user, Block block, long data) {
        if(user instanceof UUID u) {
            logBlockAction(action, u, block, data);
        } else if(user instanceof User u) {
            logBlockAction(action, u, block, data);
        } else {
            logBlockAction(action, (Entity) user, block, data);
        }
    }

    public short getBlockID(Block block) {
        int a = net.minecraft.world.level.block.Block.o.a(((CraftBlock)block).getNMS());
        return (short)a;
    }

    @Override
    public void registerAllContent() {
        actionRegistry.registerDefaultActions();
        ActionLoadEvent event = new ActionLoadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        actionRegistry.computeChecksum();
    }

    @Override
    public String getBlockName(short blockID) {
        Material material = CraftMagicNumbers.getMaterial(net.minecraft.world.level.block.Block.a(blockID).b());
        return material.name();
    }

    @Override
    public String getItemName(short itemID) {
        return null;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    @Override
    public String getEntityName(short entityID) {
        return ENTITY_MAP.get(entityID).getName();
    }

    @Override
    public int getProtocolVersion() {
        return SharedConstants.c();
    }

    public static Short2ObjectOpenHashMap<EntityType> ENTITY_MAP = new Short2ObjectOpenHashMap<>();
    public static Object2ShortOpenHashMap<EntityType> MAP = new Object2ShortOpenHashMap<>();

    // bukkit removed support for entity ids so we have to do it by hand like neanderthals
    static {
        short i = 0;
        for (EntityType type : EntityType.values()) {
            MAP.put(type, i);
            ENTITY_MAP.put(i, type);
            i++;
        }
    }
}
