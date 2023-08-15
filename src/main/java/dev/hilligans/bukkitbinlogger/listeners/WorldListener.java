package dev.hilligans.bukkitbinlogger.listeners;

import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        BukkitBinLogger.database.saveWorld(event.getWorld().getName());
        System.out.println("Saving world " + event.getWorld().getName());
        //TODO queue unloads
    }
}
