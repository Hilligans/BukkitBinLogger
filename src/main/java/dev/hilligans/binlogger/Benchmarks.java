package dev.hilligans.binlogger;

import dev.hilligans.binlogger.Action;
import dev.hilligans.binlogger.Queries;
import dev.hilligans.binlogger.Region;
import dev.hilligans.binlogger.User;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;

public class Benchmarks {
    public static void main(String[] args) {
        System.out.println(Instant.now().atZone(ZoneId.of("UTC")).toLocalDate().toString());
        System.out.println((short) (1 | 0b11 << 14));
        System.out.println(Integer.toBinaryString(Short.toUnsignedInt((short) (1 | 0b11 << 14))));
        Region region = new Region(0, 0, "world", 0, 0);
        Random random = new Random();
        long s = System.currentTimeMillis();
        for(int x = 0; x < 1000; x++) {
            long t = System.currentTimeMillis();
            for(int i = 0; i < 100; i++) {
                region.write(Action.BREAK, User.WATER.getID(), random.nextInt(2048), 0, random.nextInt(2048), t, 10, false);
            }
        }
        //region.save();
        int[] vals = new int[1000000];


        //Region region1 = Region.createMappedRegion(new File("binlogger/worlds/world/" + 0 + "_" + 0 + "/2023-06-10-0.dat"));
        long time = benchmark(region, vals);
        //long time1 = benchmark(region1, vals);
        long time1 = benchmark1(region, vals);
        System.out.println("Time for first " + time + " time for second " + time1);


    }

    public static long benchmark(Region region, int[] vals) {
        System.out.println("Start");
        long start = System.currentTimeMillis();
        int count = region.query(null, User.WATER.getID(), 343, 0, 747, null,  null, vals, 0);
        long end = System.currentTimeMillis();
        System.out.println("Found " + count + " owners");
        return end - start;
    }

    public static long benchmark1(Region region, int[] vals) {
        System.out.println("Start");
        long start = System.currentTimeMillis();
        int count = Queries.queryForUserPosition(region, User.WATER.getID(), 343, 0, 747, vals, 0, 0);
        long end = System.currentTimeMillis();
        System.out.println("Found " + count + " owners");
        return end - start;
    }
}