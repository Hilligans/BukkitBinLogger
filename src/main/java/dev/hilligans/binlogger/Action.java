package dev.hilligans.binlogger;

import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;

import java.util.function.BiFunction;

public abstract class Action {



    public static final Action NOOP = new EmptyAction("NOOP", "BinaryLogger", "", "", null);
    public static final Action TIME_HEADER = new EmptyAction("TIME_HEADER", "BinaryLogger", "", "", null);
    public static final Action BREAK = new BlockAction("BREAK", "BinaryLogger", "-", "broke", Database.BLOCK_PARSER);
    public static final Action PLACE = new PlaceAction("PLACE", "BinaryLogger", "+", "placed", Database.BLOCK_PARSER);
    public static final Action REPLACE = new BlockAction("REPLACE", "BinaryLogger", "_", "replaced", null);
    public static final Action DROP = new EmptyAction("DROP", "BinaryLogger", "+", "dropped", null);
    public static final Action KILL = new EmptyAction("KILL", "BinaryLogger", "-", "killed", null);
    public static final Action SPAWN = new EmptyAction("SPAWN", "BinaryLogger", "+", "spawned", null);
    public static final Action IGNITE = new EmptyAction("IGNITE", "BinaryLogger", "-", "ignited", Database.BLOCK_PARSER);
    public static final Action SHEAR = new EmptyAction("SHEAR", "BinaryLogger", "+", "sheared", null);
    public static final Action TAKE = new EmptyAction("TAKE", "BinaryLogger", "-", "took", null);
    public static final Action DECAY = new BlockAction("DECAY", "BinaryLogger", "-", "decayed", Database.BLOCK_PARSER);
    public static final Action INTERACT = new EmptyAction("INTERACT", "BinaryLogger", "+", "interacted", Database.BLOCK_PARSER);
    public static final Action TELEPORT = new EmptyAction("TELEPORT", "BinaryLogger", "_", "teleported", null);
    public static final Action MODIFY_SIGN = new EmptyAction("MODIFY_SIGN", "BinarryLogger", "+", "changed ", Database.BLOCK_PARSER);

    short index;
    final String icon;
    final String word;
    final String actionName;
    final String actionOwner;
    final BiFunction<Long, Database, String> parser;

    Action(String actionName, String actionOwner, String icon, String word, BiFunction<Long, Database, String> parser) {
        this.actionName = actionName;
        this.actionOwner = actionOwner;
        this.icon = icon;
        this.word = word;
        this.parser = parser;
    }

    String getActionName() {
        return actionName;
    }

    String getActionOwner() {
        return actionOwner;
    }

    short getID() {
        return index;
    }

    public abstract <T> T getRollback(Database.DatabaseEntry databaseEntry, Class<T> tClass);


    public static class BlockAction extends Action {

        BlockAction(String actionName, String actionOwner, String icon, String word, BiFunction<Long, Database, String> parser) {
            super(actionName, actionOwner, icon, word, parser);
        }

        @Override
        public <T> T getRollback(Database.DatabaseEntry databaseEntry, Class<T> tClass) {
            if(tClass.equals(IBlockData.class)) {
                return (T) ((CraftBlockData)databaseEntry.getBlockState(BukkitBinLogger.database)).getState();
            }
            return null;
        }
    }


    public static class PlaceAction extends Action {

        PlaceAction(String actionName, String actionOwner, String icon, String word, BiFunction<Long, Database, String> parser) {
            super(actionName, actionOwner, icon, word, parser);
        }

        @Override
        public <T> T getRollback(Database.DatabaseEntry databaseEntry, Class<T> tClass) {
            if(tClass.equals(IBlockData.class)) {
                return (T) ((CraftBlockData) Material.AIR.createBlockData()).getState();
            }
            return null;
        }
    }

    public static class EmptyAction extends Action {

        EmptyAction(String actionName, String actionOwner, String icon, String word, BiFunction<Long, Database, String> parser) {
            super(actionName, actionOwner, icon, word, parser);
        }

        @Override
        public <T> T getRollback(Database.DatabaseEntry databaseEntry, Class<T> tClass) {
            return null;
        }
    }
}
