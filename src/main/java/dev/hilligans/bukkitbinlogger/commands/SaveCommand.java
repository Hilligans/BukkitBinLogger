package dev.hilligans.bukkitbinlogger.commands;

import dev.hilligans.binlogger.Region;
import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        for(Region region : BukkitBinLogger.database.regionMap.values()) {
            region.save();
        }
        return false;
    }
}
