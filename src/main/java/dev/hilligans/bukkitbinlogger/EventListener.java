package dev.hilligans.bukkitbinlogger;

import dev.hilligans.binlogger.Database;
import dev.hilligans.binlogger.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

    static BukkitDatabase database = BukkitBinLogger.database;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBellRing(BellRingEvent event) {
        long id = database.getBlockID(event.getBlock());
        //database.logBlockAction(dev.hilligans.binlogger.Action.INTERACT, User.FIRE, event.getBlock(), id << (4 * 8));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        database.logBlockBreak(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        long id = database.getBlockID(event.getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.BREAK, User.FIRE, event.getBlock(), id << (4 * 8));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCook(BlockCookEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDispense(BlockCookEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDropItem(BlockDropItemEvent event) {
        //database.logBlockAction(dev.hilligans.binlogger.Action.DROP, );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {
        for(Block block : event.blockList()) {
            database.logBlockBreak(User.EXPLOSION, block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFade(BlockFadeEvent event) {
        database.logBlockReplace(User.ENVIRONMENT, event.getBlock(), event.getNewState().getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFertilized(BlockFertilizeEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFormed(BlockFormEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFromTo(BlockFromToEvent event) {
        long oldID = database.getBlockID(event.getBlock());
        long newID = database.getBlockID(event.getToBlock());
      //  database.logAction(dev.hilligans.binlogger.Action.PLACE, event.get, oldBlock.getX(), oldBlock.getY(), oldBlock.getZ(), oldBlock.getWorld().getName(), System.currentTimeMillis(), (oldID << (4 * 8)) | (newID << (2 * 8)) );

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockGrow(BlockGrowEvent event) {
        long id = database.getBlockID(event.getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.PLACE, User.ENVIRONMENT, event.getBlock(), id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockIgnite(BlockIgniteEvent event) {
        long id = database.getBlockID(event.getBlock());
        //System.out.println("BlockIgnite, " + event.getBlock().getX() + " " + event.getBlock().getY() + " " + event.getBlock().getZ());
        database.logBlockAction(dev.hilligans.binlogger.Action.IGNITE,
                switch (event.getCause()) {
                    case FLINT_AND_STEEL -> event.getPlayer() != null ? event.getPlayer() : User.DISPENSER;
                    case SPREAD -> User.FIRE;
                    case LIGHTNING -> User.LIGHTNING;
                    case LAVA -> User.LAVA;
                    case FIREBALL, ENDER_CRYSTAL, EXPLOSION -> User.EXPLOSION;
                    case ARROW -> event.getIgnitingEntity();
                }, event.getBlock(), id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockMultiPlaceEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(BlockMultiPlaceEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        database.logBlockPlace(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockShearEntity(BlockShearEntityEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockSpread(BlockSpreadEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityFormBlock(EntityBlockFormEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveDecayEvent(LeavesDecayEvent event) {
        long id = database.getBlockID(event.getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.BREAK, User.ENVIRONMENT, event.getBlock(), id << (4 * 8));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMoistureChange(MoistureChangeEvent event) {
        long oldID = database.getBlockID(event.getBlock());
        long newID = database.getBlockID(event.getNewState().getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.REPLACE, User.ENVIRONMENT, event.getBlock(), (oldID << (4 * 8)) | (newID << (2 * 8)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        long id = database.getBlockID(event.getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.MODIFY_SIGN, event.getPlayer(), event.getBlock(), id << (4 * 8));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        //long id = database.getBlockID(event.getBlock());
        //database.logBlockAction(dev.hilligans.binlogger.Action.MODIFY_SIGN, User.SPONGE, event.getBlock(), id << (4 * 8));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTntPrimed(TNTPrimeEvent event) {
        long id = database.getBlockID(event.getBlock());
        database.logBlockAction(dev.hilligans.binlogger.Action.IGNITE,
                switch (event.getCause()) {
                    case FIRE -> User.FIRE;
                    case REDSTONE -> User.REDSTONE;
                    case PLAYER -> event.getPrimingEntity();
                    case EXPLOSION -> User.EXPLOSION;
                    case PROJECTILE -> event.getPrimingEntity();
                    case BLOCK_BREAK -> null;
                    case DISPENSER -> User.DISPENSER;
                }, event.getBlock(), id << (4 * 8));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent deathEvent) {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if(BukkitBinLogger.instance.inspector.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            CraftBlock b = (CraftBlock) event.getClickedBlock();
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (b != null && !b.getType().equals(Material.AIR)) {
                    Location location = b.getLocation().add(event.getBlockFace().getDirection());
                    String result = BukkitBinLogger.database.parseQuery(new String[]{String.valueOf(location.getBlockX()), String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ())}, b.getWorld().getName());
                    event.getPlayer().sendMessage(result);
                }
            } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (b != null && !b.getType().equals(Material.AIR)) {
                    Location location = b.getLocation();
                    String result = BukkitBinLogger.database.parseQuery(new String[]{String.valueOf(location.getBlockX()), String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ())}, b.getWorld().getName());
                    event.getPlayer().sendMessage(result);
                }
            }
            event.setCancelled(true);
        }
    }
}
