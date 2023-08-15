package dev.hilligans.bukkitbinlogger;

import dev.hilligans.bukkitbinlogger.commands.*;
import dev.hilligans.bukkitbinlogger.listeners.BlockEventListener;
import dev.hilligans.bukkitbinlogger.listeners.InventoryEventListener;
import dev.hilligans.bukkitbinlogger.listeners.WorldListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class BukkitBinLogger extends JavaPlugin {

    public static final BukkitDatabase database = new BukkitDatabase();

    public static BukkitBinLogger instance;
    public CommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();
    public HashMap<UUID, Boolean> inspector = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        //Main.main(null);
        PluginManager manager = Bukkit.getPluginManager();

        manager.registerEvents(new BlockEventListener(), this);
        manager.registerEvents(new InventoryEventListener(), this);
        manager.registerEvents(new WorldListener(), this);

        commandMap.register("binlogger", new LookupCommand());
        commandMap.register("binlogger", new InspectorCommand());
        commandMap.register("binlogger", new RollbackCommand());

        database.registerAllContent();
    }

    @Override
    public void onDisable() {
        database.saveAll();
        // Plugin shutdown logic
    }
}
