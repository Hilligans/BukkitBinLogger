package dev.hilligans.binlogger;

import dev.hilligans.binlogger.util.Tuple;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegionContainer {

    public ArrayList<Region> regions = new ArrayList<>();

    public int x;
    public int z;
    public String worldName;

    public RegionContainer(int x, int z, String worldName) {
        this.x = x;
        this.z = z;
        this.worldName = worldName;
    }

    public void addRegion(Region region) {
        int x;
        for(x = 0; x < regions.size() && regions.get(x).startTime > region.startTime; x++);
        regions.add(x, region);
    }

    public Region getOrCreateRegion(Supplier<Region> regionSupplier) {
        if(regions.size() > 0 && regions.get(0).writing) {
            return regions.get(0);
        }
        Region region = regionSupplier.get();
        regions.add(0, region);
        return region;
    }

    public int query(Query query, QueryResult queryResult, int[] data, int dataOffset, int limit) {
        int count = 0;
        for(Region region : regions) {
            if(limit - count > 0) {
                break;
            }
            int added = region.query(query, data, dataOffset + count, limit - count);
            if(added != 0) {
                queryResult.regions.add(new Tuple<>( dataOffset + count, region));
                count += added;
            }
        }
        return count;
    }

    public void save() {
        for(Region region : regions) {
            region.save();
        }
    }

    public void saveHeader() {
        for(Region region : regions) {
            region.saveHeader();
        }
    }

    public void readHeader() {

    }

    public void rebuildHeader() {
        File folder = new File(Save.getSaveDirectory(worldName, x, z, ""));
        ArrayList<String> names = new ArrayList<>();
        out:
        {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files == null) {
                    break out;
                }
                for (File f : files) {
                    if (f.getName().startsWith("log-")) {
                        names.add(f.getName());
                    }
                }
            } else {
                throw new RuntimeException("");
            }
        }
    }
}
