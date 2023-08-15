package dev.hilligans.bukkitbinlogger.listeners;

import dev.hilligans.binlogger.Action;
import dev.hilligans.bukkitbinlogger.BukkitBinLogger;
import dev.hilligans.bukkitbinlogger.BukkitDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryEventListener implements Listener {

    static BukkitDatabase database = BukkitBinLogger.database;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(final InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        if(inventory.getHolder() != null) {
            if (inventory.getHolder() instanceof Player) {
                if (event.getWhoClicked().equals(event.getInventory().getHolder())) {
                    return;
                }
            }
            Location location = inventory.getLocation();
            if (location == null) {
                return;
            }

            if (inventory.getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                Block block = blockInventoryHolder.getBlock();
                if(event.getNewItems().entrySet().size() == 1) {
                    Map.Entry<Integer, org.bukkit.inventory.ItemStack> stacks = event.getNewItems().entrySet().iterator().next();
                    int slot = stacks.getKey();
                    if (inventory.getSize() > slot) {
                        ItemStack s = inventory.getItem(slot);
                        int count = stacks.getValue().getAmount();
                        if (s != null) {
                            count -= s.getAmount();
                        }
                        database.logItemAction(Action.ADD, player, stacks.getValue(), block, slot, count, false);
                    }
                } else {
                    int totalCount = 0;
                    ItemStack stack = null;
                    for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> stacks : event.getNewItems().entrySet()) {
                        int slot = stacks.getKey();
                        stack = stacks.getValue();
                        if (inventory.getSize() > slot) {
                            ItemStack s = inventory.getItem(slot);
                            int count = stacks.getValue().getAmount();
                            if (s != null) {
                                count -= s.getAmount();
                            }
                            totalCount += count;
                            database.logItemAction(Action.ADD, player, stacks.getValue(), block, slot, count, true);
                        }
                    }
                    if(stack != null && totalCount != 0) {
                        database.logBulkItemAction(Action.BULK_ADD, player, stack, block, totalCount);
                    }
                }
            }
        } else {
            throw new RuntimeException("Yet to implement");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        int slot = event.getRawSlot(); //this is the unique slot number for the view.
        // Specifically slot -999, or out of the window
        if (slot < 0) {
            return;
        }
        Location containerLoc = event.getInventory().getLocation(); //this is the top Inventory
        // Virtual inventory or something (enderchest?)
        if (containerLoc == null) {
            return;
        }

        // Store some info
        final Player player = (Player) event.getWhoClicked();

        // Ignore all item move events where players modify their own inventory
        if (event.getInventory().getHolder() instanceof Player) {
            Player other = (Player) event.getInventory().getHolder();

            if (other.equals(player)) {
                return;
            }
        }
        boolean isTopInv = slot < event.getInventory().getSize();

        ItemStack heldItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();

        // This happens when opening someone else's inventory, so don't bother tracking it
        if (slotItem == null) {
            return;
        }


        switch (event.getClick()) {
            // IGNORE BOTTOM
            case LEFT:
                if (isTopInv) {
                    if (heldItem == null || heldItem.getType() == Material.AIR) {
                        if (slotItem.getType() != Material.AIR) {
                            if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                                Block block = blockInventoryHolder.getBlock();
                                database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount(), false);
                            } else {
                                throw new RuntimeException("Not Implemented yet");
                            }
                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem,
                            //        slotItem.getAmount(), slot, null, containerLoc, player));
                           // Prism.debug("ACTION: " + event.getAction().name());
                        }
                    } else {
                        int amount = 0;
                        int maxStack = heldItem.getMaxStackSize();
                        if (slotItem.getType() == Material.AIR && heldItem.getAmount() <= maxStack) {
                            amount = heldItem.getAmount();
                        }
                        if (slotItem.getType().equals(heldItem.getType())) {
                            int slotQty = slotItem.getAmount();
                            amount = Math.min(maxStack - slotQty, heldItem.getAmount());
                        }
                        if (amount > 0) {
                            if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                                Block block = blockInventoryHolder.getBlock();
                                database.logItemAction(Action.ADD, player, heldItem, block, slot, amount, false);
                            } else {
                                throw new RuntimeException("Not Implemented yet");
                            }
                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(INSERT, heldItem, amount, slot,
                            //        null, containerLoc, player));
                            //Prism.debug("ACTION: " + event.getAction().name());

                        }
                        if (slotItem.getType() != Material.AIR && !slotItem.getType().equals(heldItem.getType())) {
                            // its a switch.
                            if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                                Block block = blockInventoryHolder.getBlock();
                                database.logItemAction(Action.ADD, player, heldItem, block, slot, heldItem.getAmount(), false);
                                database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount(), false);
                            } else {
                                throw new RuntimeException("Not Implemented yet");
                            }

                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(INSERT, heldItem,
                            //        heldItem.getAmount(), slot, null, containerLoc, player));
                            //Prism.debug("ACTION: " + event.getAction().name());
                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem,
                            //        slotItem.getAmount(), slot, null, containerLoc, player));
                        }
                    }
                }
                break;

            case RIGHT:
                if (isTopInv) {
                    if (heldItem == null || heldItem.getType() == Material.AIR) {
                        if (slotItem.getType() != Material.AIR) {
                            if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                                Block block = blockInventoryHolder.getBlock();
                                database.logItemAction(Action.TAKE, player, slotItem, block, slot, (slotItem.getAmount() + 1) / 2, false);
                            } else {
                                throw new RuntimeException("Not Implemented yet");
                            }
                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem,
                            //        (slotItem.getAmount() + 1) / 2, slot, null, containerLoc, player));
                            //Prism.debug("ACTION: " + event.getAction().name());

                        }
                    } else {
                        if ((slotItem.getType() == Material.AIR || slotItem.equals(heldItem))
                                && slotItem.getAmount() < slotItem.getType().getMaxStackSize()) {
                            if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                                Block block = blockInventoryHolder.getBlock();
                                database.logItemAction(Action.ADD, player, slotItem, block, slot, 1, false);
                            } else {
                                throw new RuntimeException("Not Implemented yet");
                            }
                            //RecordingQueue.addToQueue(ActionFactory.createItemStack(INSERT, slotItem, 1, slot, null,
                            //        containerLoc, player));
                            //Prism.debug("ACTION: " + event.getAction().name());

                        }
                    }
                }
                break;

            case NUMBER_KEY:
                if (isTopInv) {
                    ItemStack swapItem = player.getInventory().getItem(event.getHotbarButton());

                    if (slotItem.getType() != Material.AIR) {
                        if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                            Block block = blockInventoryHolder.getBlock();
                            database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount(), false);
                        } else {
                            throw new RuntimeException("Not Implemented yet");
                        }
                       // RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem, slotItem.getAmount(),
                       //         slot, null, containerLoc, player));
                       // Prism.debug("ACTION: " + event.getAction().name());

                    }

                    if (swapItem != null && swapItem.getType() != Material.AIR) {
                        if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                            Block block = blockInventoryHolder.getBlock();
                            database.logItemAction(Action.ADD, player, swapItem, block, slot, swapItem.getAmount(), false);
                           // database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount());
                        } else {
                            throw new RuntimeException("Not Implemented yet");
                        }
                        //RecordingQueue.addToQueue(ActionFactory.createItemStack(INSERT, swapItem, swapItem.getAmount(),
                        //        slot, null, containerLoc, player));
                        //Prism.debug("ACTION: " + event.getAction().name());

                    }
                }
                break;

            // HALF 'N HALF
            case DOUBLE_CLICK: {
                throw new RuntimeException("Not Yet Implemented");
                /*
                int amount = (heldItem == null) ? 0 :
                        heldItem.getType().getMaxStackSize() - heldItem.getAmount();

                ItemStack[] contents = event.getInventory().getStorageContents();
                int length = contents.length;

                for (int i = 0; i < length; ++i) {
                    ItemStack is = contents[i];

                    int size = 0;
                    if (is != null && (is.getType() != Material.AIR || is.equals(heldItem))) {
                        size += is.getAmount();
                    }
                    amount = recordDeductTransfer(REMOVE, size, amount, heldItem, containerLoc, i, player, event);
                    if (amount <= 0) {
                        break;
                    }
                }

                 */
                //break;
            }

            // CROSS INVENTORY EVENTS
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                throw new RuntimeException("Not Yet Implemented");
                /*
                if (isTopInv) {
                    if (slotItem.getType() != Material.AIR) {
                        int stackSize = slotItem.getType().getMaxStackSize();
                        int remaining = slotItem.getAmount();

                        for (ItemStack is : event.getView().getBottomInventory().getStorageContents()) {
                            //noinspection ConstantConditions  Until intellij sorts it checks
                            if (is == null || is.getType() == Material.AIR) {
                                remaining -= stackSize;
                            } else if (is.isSimilar(slotItem)) {
                                remaining -= (stackSize - Math.min(is.getAmount(), stackSize));
                            }

                            if (remaining <= 0) {
                                remaining = 0;
                                break;
                            }
                        }
                        if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                            Block block = blockInventoryHolder.getBlock();
                            database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount());
                        } else {
                            throw new RuntimeException("Not Implemented yet");
                        }

                        //RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem,
                       //         slotItem.getAmount() - remaining, slot, null, containerLoc, player));
                        //Prism.debug("ACTION: " + event.getAction().name());

                    }
                } else {
                    int stackSize = slotItem.getType().getMaxStackSize();
                    int amount = slotItem.getAmount();

                    ItemStack[] contents = event.getInventory().getStorageContents();
                    int length = contents.length;

                    // Fill item stacks first
                    for (int i = 0; i < length; ++i) {
                        ItemStack is = contents[i];

                        if (slotItem.isSimilar(is)) {
                            amount = recordDeductTransfer(INSERT, stackSize - is.getAmount(), amount, slotItem,
                                    containerLoc, i, player, event);
                            if (amount <= 0) {
                                break;
                            }
                        }
                    }

                    // Fill empty slots
                    if (amount > 0) {
                        for (int i = 0; i < length; ++i) {
                            ItemStack is = contents[i];

                            if (is == null || is.getType() == Material.AIR) {
                                amount = recordDeductTransfer(INSERT, stackSize, amount, slotItem,
                                        containerLoc, i, player, event);
                                if (amount <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                 */
                //break;

            // DROPS
            case DROP:
                if (slotItem.getType() != Material.AIR && slotItem.getAmount() > 0) {
                    if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                        Block block = blockInventoryHolder.getBlock();
                        database.logItemAction(Action.TAKE, player, slotItem, block, slot, 1, false);
                    } else {
                        throw new RuntimeException("Not Implemented yet");
                    }
                    //RecordingQueue.addToQueue(
                    //        ActionFactory.createItemStack(REMOVE, slotItem, 1, slot, null, containerLoc, player));
                    //Prism.debug("ACTION: " + event.getAction().name());

                }
                break;

            case CONTROL_DROP:
                if (slotItem.getType() != Material.AIR && slotItem.getAmount() > 0) {
                    if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
                        Block block = blockInventoryHolder.getBlock();
                        database.logItemAction(Action.TAKE, player, slotItem, block, slot, slotItem.getAmount(), false);
                    } else {
                        throw new RuntimeException("Not Implemented yet");
                    }
                    //RecordingQueue.addToQueue(ActionFactory.createItemStack(REMOVE, slotItem, slotItem.getAmount(),
                    //        slot, null, containerLoc, player));
                    //Prism.debug("ACTION: " + event.getAction().name());

                }
                break;

            case WINDOW_BORDER_LEFT:
                // Drop stack on cursor
            case WINDOW_BORDER_RIGHT:
                // Drop 1 on cursor

            default:
                // What the hell did you do
        }
    }
}
