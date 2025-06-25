package dev.hilligans.binlogger.data.structure;

import dev.hilligans.binlogger.data.BinaryFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DataTree {

    public static final String WORLD_PATH = "world/";
    public static final String VERSION_PATH = "version/";



    public ArrayList<BinaryFile> binaryFiles = new ArrayList<>();
    
    public HashMap<String, World> worlds = new HashMap<>();




    public DataTree(File path) {
        File world = new File(path.getPath() + WORLD_PATH);
        File versions = new File(path.getPath() + VERSION_PATH);

        world.mkdir();
        versions.mkdir();



    }
}
