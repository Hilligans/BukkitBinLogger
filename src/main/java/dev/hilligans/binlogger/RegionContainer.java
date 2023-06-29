package dev.hilligans.binlogger;

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

    public int query(Query query, int[] data, int dataOffset, int limit) {
        int count = 0;
        for(Region region : regions) {
            if(limit - count > 0) {
                break;
            }
            count += region.query(query, data, dataOffset + count, limit - count);
        }
        return count;
    }

    public void save() {
        for(Region region : regions) {
            region.save();
        }
    }
}
