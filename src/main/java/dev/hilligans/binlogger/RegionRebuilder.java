package dev.hilligans.binlogger;

import com.google.common.collect.Comparators;
import org.apache.commons.lang3.tuple.Triple;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;

public class RegionRebuilder {

    public Region reorganizeRegion(Region region) {
        Region newRegion = new Region(region.x, region.z, region.worldName, region.protocolVersion, region.actionChecksum);



        return null;
    }

    public static Region mergeRegions(Region region1, Region region2) {
        if(region1.getSizeInBytes() + region2.getSizeInBytes() > Integer.MAX_VALUE) {
            throw new RuntimeException("New region is too large to be able to be memory mapped, once the standard library can take advantage fully of panama this will no longer be an issue");
        }
        PlayerTable newTable = mergedSorted(region1.playerTable, region2.playerTable);
        Region region = new Region(region1, (region1.bufferPointer + region2.bufferPointer) * 16, newTable);

        int region1Index = 0;
        int region2Index = 0;

        long region1Time = 0;
        long region2Time = 0;

        while(region1Index < region1.bufferPointer && region2Index < region2.bufferPointer) {
            if(region1Index % region1.timeHeaderOffset == 0) {
                region1Time = region1.byteBuffer.getLong(region1Index * 16 + 8);
            }
            if(region2Index % region2.timeHeaderOffset == 0) {
                region2Time = region2.byteBuffer.getLong(region2Index * 16 + 8);
            }
            long time1 = Region.calculateTime(region1.byteBuffer.getShort(region1Index * 16 + 8), region1Time);
            long time2 = Region.calculateTime(region2.byteBuffer.getShort(region2Index * 16 + 8), region2Time);

            //greater time means newer
            if(time1 > time2) {
                region2Index = getRegionIndex(region2, newTable, region, region2Index, time2);
            } else {
                region1Index = getRegionIndex(region1, newTable, region, region1Index, time2);
            }
        }
        if(region1Index < region1.bufferPointer) {
            moveRestForRegion(region1, region, newTable, region1Index, region1Time);
        } else {
            moveRestForRegion(region2, region, newTable, region2Index, region2Time);
        }

        return region;
    }

    private static void moveRestForRegion(Region oldRegion, Region region, PlayerTable newTable, int regionIndex, long regionTime) {
        while(regionIndex < oldRegion.bufferPointer) {
            if(regionIndex % oldRegion.timeHeaderOffset == 0) {
                regionTime = oldRegion.byteBuffer.getLong(regionIndex * 16 + 8);
            }
            long time = Region.calculateTime(oldRegion.byteBuffer.getShort(regionIndex * 16 + 8), regionTime);
            regionIndex = getRegionIndex(oldRegion, newTable, region, regionIndex, time);

        }
    }

    private static final long mask = ~(0xFFFFL << 48);
    private static final long mask1 = ~(0xFFFFL << 32);
    private static final long mask2 = (0xFFFFL << 32);

    private static int getRegionIndex(Region oldRegion, PlayerTable newTable, Region region, int regionIndex, long time2) {
        long val = oldRegion.byteBuffer.getLong(regionIndex * 16);
        region.write(((val & mask1) | newTable.map.getShort(oldRegion.playerTable.players.get((int) ((val & mask2) >> 32)))), time2, oldRegion.byteBuffer.getLong(regionIndex * 16 + 8) & mask);
        return ++regionIndex;
    }


    public static PlayerTable mergedSorted(PlayerTable playerTable1, PlayerTable playerTable2) {
        ArrayList<UUID> players = new ArrayList<>(playerTable1.players.size() + playerTable2.players.size());

        players.addAll(playerTable1.players);
        players.addAll(playerTable2.players);
        players.sort(UUID::compareTo);

        ArrayList<UUID> newList = new ArrayList<>(Math.max(playerTable1.players.size(), playerTable2.players.size()));
        for(int x = 0; x < players.size(); x++) {
            if(x + 1 < players.size() || !players.get(x).equals(players.get(x + 1))) {
                newList.add(players.get(x));
            }
        }

        if(newList.size() >= 65536) {
            throw new RuntimeException("Combined player table is too large to represent all players!");
        }
        return new PlayerTable(newList, true);
    }

}
