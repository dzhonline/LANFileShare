package com.dzh.game_2048;

public class AnimatedCell {
    public int fromX, fromY;
    public int toX, toY;
    public float animX, animY;
    public int value;
    public boolean merging;

    public AnimatedCell(int fromX, int fromY, int toX, int toY, int value, boolean merging) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.value = value;
        this.merging = merging;
        this.animX = fromX;
        this.animY = fromY;
    }
}