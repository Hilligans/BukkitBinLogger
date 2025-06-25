package dev.hilligans.binlogger;

import dev.hilligans.binlogger.util.FileLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class Parser {

    public static void main(String[] args) {
        JSONArray array = new JSONArray(FileLoader.readString("/blocks.json"));
        int counter = 0;
        for(int x = 0; x < array.length(); x++) {
            JSONObject block = array.getJSONObject(x);
            int base = 1;

            JSONArray arr = block.getJSONArray("states");
            for(int y = 0; y < arr.length(); y++) {
                JSONObject state = arr.getJSONObject(y);
                base *= state.getInt("num_values");
            }

            counter += base;
        }

        System.out.println(counter);
    }
}
