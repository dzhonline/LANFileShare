package com.dzh.game_2048;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListener extends GestureDetector.SimpleOnGestureListener {
    private final SwipeCallback callback;
    private static final int SWIPE_THRESHOLD = 50;

    public interface SwipeCallback {
        void onSwipe(String direction);
    }

    public SwipeListener(SwipeCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
        float dx = e2.getX() - e1.getX();
        float dy = e2.getY() - e1.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            callback.onSwipe(dx > 0 ? "right" : "left");
        } else {
            callback.onSwipe(dy > 0 ? "down" : "up");
        }

        return true;
    }
}