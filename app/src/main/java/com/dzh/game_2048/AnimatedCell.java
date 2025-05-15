package com.dzh.game_2048;

public class AnimatedCell {
    public int fromX, fromY;
    public int toX, toY;
    public float animX, animY;
    public int fromValue, toValue;

    public AnimatedCell(int fromX, int fromY, int toX, int toY, int fromValue, int toValue) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.animX = fromY;
        this.animY = fromX;
    }
}