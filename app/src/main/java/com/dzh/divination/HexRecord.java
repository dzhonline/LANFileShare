package com.dzh.divination;

public class HexRecord {
    public String time;
    public String name;
    public String key;

    public HexRecord(String time, String name, String key) {
        this.time = time;
        this.name = name;
        this.key = key;
    }

    @Override
    public String toString() {
        return time + " [" + name + "]";
    }
}