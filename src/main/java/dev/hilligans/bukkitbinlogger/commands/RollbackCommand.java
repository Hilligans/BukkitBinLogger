package dev.hilligans.bukkitbinlogger.commands;

import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import dev.hilligans.bukkitbinlogger.rollback.RollBack;
import dev.hilligans.bukkitbinlogger.rollback.RollbackPreview;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.world.level.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class RollbackCommand extends Command {

    public HashMap<UUID, RollBack> rollbacks = new HashMap<>();

    public RollbackCommand() {
        super("rollback");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender instanceof Player player) {
            if (args[0].equals("confirm")) {
                rollbacks.get(player.getUniqueId()).apply();
                rollbacks.remove(player.getUniqueId());
                sender.sendMessage("Rollback Confirmed");
            } else if (args[0].equals("cancel")) {
                RollBack rollBack = rollbacks.remove(player.getUniqueId());
                if(rollBack != null) {
                    RollbackPreview.sendCleared(rollBack);
                }
                sender.sendMessage("Rollback canceled");
            } else if (args[0].equals("preview")) {
                String[] args1 = new String[args.length - 1];
                System.arraycopy(args, 1, args1, 0, args1.length);
                RollBack rollBack = BukkitBinLogger.database.getRollback(args1, ((CraftWorld) ((Player) sender).getWorld()).getHandle());
                rollBack.build();
                RollbackPreview rollbackPreview = new RollbackPreview(rollBack);
                rollbacks.put(player.getUniqueId(), rollBack);
                rollbackPreview.sendPreview();

                TextComponent confirm = new TextComponent("[Confirm]");
                confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/binlogger:rollback confirm"));
                confirm.setColor(ChatColor.DARK_GREEN);
                confirm.setBold(true);

                TextComponent cancel = new TextComponent("[Cancel]");
                cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/binlogger:rollback cancel"));
                cancel.setColor(ChatColor.DARK_RED);
                cancel.setBold(true);

                BaseComponent[] components = new ComponentBuilder("Previewing Rollback\n").color(ChatColor.of(new Color(157, 157, 157))).bold(true).append(confirm).append(" ").append(cancel).create();

                sender.spigot().sendMessage(components);
            }
        }
        return true;
    }
}