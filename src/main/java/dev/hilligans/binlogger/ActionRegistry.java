package dev.hilligans.binlogger;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ActionRegistry {

    public ArrayList<Action> actions = new ArrayList<>();
    public long checksum;
    boolean computedChecksum = false;

    public ActionRegistry() {

    }

    public ActionRegistry(String path) {
        InputStream stream = Save.class.getResourceAsStream(path);
        if(stream == null) {
            throw new RuntimeException("Failed to load file " + path);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.lines().forEach(string -> {
            String[] parts = string.split(":", 2);
            actions.add(new Action.EmptyAction(parts[0], parts[1], null, null, null));
        });
    }


    /**
     * Gets the conversion table for converting this action registry to the one provided
     */
    public int[] getConversionTable(ActionRegistry actionRegistry) {
        int[] vals = new int[actions.size()];
        int index = 0;
        for(Action action : actions) {
            Action newAction = actionRegistry.getEquivalent(action);
            if(newAction != null) {
                vals[index] = newAction.getID();
            } else {
                vals[index] = 0;
            }
            index++;
        }
        return vals;
    }

    public Action getEquivalent(Action action) {
        for(Action action1 : actions) {
            if(action1.actionName.equals(action.actionName)) {
                if(action1.actionOwner.equals(action.actionOwner)) {
                    return action1;
                }
            }
        }
        return null;
    }

    public void save(String path) {
        StringBuilder builder = new StringBuilder();
        for(Action action : actions) {
            builder.append(action.getActionName()).append(":").append(action.getActionOwner()).append("\n");
        }
        Save.saveString(path + "/" + checksum + ".checksum.txt", builder.toString());
    }

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
