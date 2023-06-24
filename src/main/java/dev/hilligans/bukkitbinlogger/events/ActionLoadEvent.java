package dev.hilligans.bukkitbinlogger.events;

import dev.hilligans.binlogger.Action;
import dev.hilligans.binlogger.ActionRegistry;
import dev.hilligans.bukkitbinlogger.BukkitDatabase;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ActionLoadEvent extends Event {

    public BukkitDatabase database;

    private static final HandlerList handlers = new HandlerList();

    public ActionLoadEvent(BukkitDatabase bukkitDatabase) {
        this.database = bukkitDatabase;
    }

    public BukkitDatabase getDatabase() {
        return database;
    }

    public ActionRegistry getActionRegistry() {
        return database.actionRegistry;
    }

    public void registerAction(Action action) {
        database.actionRegistry.registerAction(action);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
