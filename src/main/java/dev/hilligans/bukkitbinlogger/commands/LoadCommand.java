package dev.hilligans.bukkitbinlogger.commands;

import dev.hilligans.binlogger.Database;
import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoadCommand extends Command {
    public LoadCommand() {
        super("load");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        BukkitBinLogger.database.loadActiveTablesToMemory(((Player)sender).getWorld().getName());
        return true;
    }
}
