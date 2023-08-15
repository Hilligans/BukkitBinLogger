package dev.hilligans.binlogger;

import dev.hilligans.binlogger.util.Tuple;

import java.nio.ByteBuffer;
import java.util.*;

public class QueryResult {

    public ArrayList<Tuple<Integer, Region>> regions = new ArrayList<>();
    public int[] vals;
    public Query query;

    public QueryResult(Query query) {
        this.query = query;
    }

    public QueryResult setData(int[] vals) {
        this.vals = vals;
        return this;
    }

    public void parseResults() {
        Integer[] vals = new Integer[regions.size()];
        for(int x = 0; x < regions.size(); x++) {
            vals[x] = x;
        }
        Arrays.sort(vals, (o1, o2) -> {
            int index1 = regions.get(o1).t;
            int index2 = regions.get(o2).t;

            Region region1 = regions.get(o1).q;
            Region region2 = regions.get(o2).q;

            return Long.compare(region1.getTime(vals[index1]), region2.getTime(vals[index2]));
        });

        ByteBuffer buffer = ByteBuffer.allocate(regions.size() * 8);
        for(int x = 0; x < regions.size(); x++) {
            int val = vals[x];
            buffer.putInt(val * 8, regions.get(val).t);
            buffer.putInt(val * 8 + 4, val);
        }

        Database.DatabaseEntry[] entries = new Database.DatabaseEntry[query.perRegionQuery];
        int index = 0;
        while(index != entries.length) {
            //int val = vals[]
            //entries[index] = new Database.DatabaseEntry(regions.get(buffer.getInt(4)))



            index++;
        }
    }



}
