package dev.hilligans.bukkitbinlogger.commands;

import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class LookupCommand extends Command {

    public LookupCommand() {
        super("lookup");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        //String result = BukkitBinLogger.database.parseQuery(args);
        //sender.sendMessage(result);
        return true;
    }
}
