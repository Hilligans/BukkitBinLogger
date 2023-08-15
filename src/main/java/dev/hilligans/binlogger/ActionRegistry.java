package dev.hilligans.binlogger;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;

import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ActionRegistry {

    public ArrayList<Action> actions = new ArrayList<>();
    public long checksum;
    boolean computedChecksum = false;

    public Action getAction(int index) {
        return actions.get(index);
    }

    public void registerAction(Action action) {
        action.index = (short) this.actions.size();
        this.actions.add(action);
    }

    public long getChecksum() {
        if(!computedChecksum) {
            throw new RuntimeException("Checksum not computed yet!");
        }
        return checksum;
    }

    public void computeChecksum() {
        Checksum crc32 = new CRC32();
        ByteArrayList list = new ByteArrayList();

        for(Action action : actions) {
            String key = action.getActionOwner() + ":" + action.getActionName();
            list.ensureCapacity(list.size() + key.length());
            for(int x = 0; x < key.length(); x++) {
                list.add((byte)key.charAt(x));
            }
        }

        crc32.update(list.elements(), 0, list.size());
        this.checksum = crc32.getValue();
        this.computedChecksum = true;
    }

    public void registerDefaultActions() {
        registerAction(Action.NOOP);
        registerAction(Action.TIME_HEADER);
        registerAction(Action.BREAK);
        registerAction(Action.PLACE);
        registerAction(Action.REPLACE);
        registerAction(Action.DROP);
        registerAction(Action.KILL);
        registerAction(Action.SPAWN);
        registerAction(Action.IGNITE);
        registerAction(Action.SHEAR);
        registerAction(Action.TAKE);
        registerAction(Action.ADD);
        registerAction(Action.DECAY);
        registerAction(Action.INTERACT);
        registerAction(Action.TELEPORT);
        registerAction(Action.MODIFY_SIGN);
        registerAction(Action.BULK_TAKE);
        registerAction(Action.BULK_ADD);
    }
}
