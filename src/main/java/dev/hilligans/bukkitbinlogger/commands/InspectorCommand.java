package dev.hilligans.bukkitbinlogger.commands;

import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InspectorCommand extends Command {

    public InspectorCommand() {
        super("inspect");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            boolean val = !BukkitBinLogger.instance.inspector.getOrDefault(player.getUniqueId(), false);
            BukkitBinLogger.instance.inspector.put(player.getUniqueId(), val);
            return true;
        } else {
            return false;
        }
    }
}
