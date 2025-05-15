package com.dzh.game_2048;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.dzh.R;

import java.util.ArrayList;
import java.util.List;

public class Game2048View extends View {
    private final Paint paint = new Paint();
    private final int gridSize = 4;
    private final Game2048Model model;
    private final GestureDetector gestureDetector;
    private SwipeListener.SwipeCallback externalCallback;

    private final List<AnimatedCell> animatedCells = new ArrayList<>();
    private boolean isAnimating = false;

    // 扩展颜色表（可继续扩展）
    int[] colorResList = {
            R.color.num_2, R.color.num_4, R.color.num_8,
            R.color.num_16, R.color.num_32, R.color.num_64,
            R.color.num_128, R.color.num_256, R.color.num_512,
            R.color.num_1024, R.color.num_2048,
            R.color.num_4096, R.color.num_8192, R.color.num_16384
    };

    public Game2048View(Context context, AttributeSet attrs) {
        super(context, attrs);
        model = new Game2048Model(gridSize);
        gestureDetector = new GestureDetector(context, new SwipeListener(this::onSwipe));
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public Game2048Model getModel() {
        return model;
    }

    public void setSwipeCallback(SwipeListener.SwipeCallback callback) {
        this.externalCallback = callback;
    }

    private void onSwipe(String direction) {
        if (!isAnimating && externalCallback != null) {
            externalCallback.onSwipe(direction);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cellSize = getWidth() / gridSize;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(cellSize * 0.4f);
        paint.setAntiAlias(true);

        // ✅ 每格都先绘制背景，避免动画覆盖不完整
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                drawCellBackground(canvas, i, j, cellSize);

                if (!isAnimating || !isCellInAnimation(i, j)) {
                    drawCellForeground(canvas, i, j, model.grid[i][j], cellSize);
                }
            }
        }

        // ✅ 动画中的格子（绘在移动轨迹上）
        for (AnimatedCell animCell : animatedCells) {
            float drawX = animCell.animX * cellSize;
            float drawY = animCell.animY * cellSize;
            drawAnimatedCell(canvas, animCell.value, drawX, drawY, cellSize);
        }
    }

    private void drawCellBackground(Canvas canvas, int i, int j, int cellSize) {
        float left = j * cellSize;
        float top = i * cellSize;
        paint.setColor(ContextCompat.getColor(getContext(), R.color.bg_block));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);
    }

    private void drawCellForeground(Canvas canvas, int i, int j, int value, int cellSize) {
        if (value == 0) return;
        float left = j * cellSize;
        float top = i * cellSize;

        int index = (int) (Math.log(value) / Math.log(2)) - 1;
        int colorIndex = index % colorResList.length;
        paint.setColor(ContextCompat.getColor(getContext(), colorResList[colorIndex]));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(value),
                left + cellSize / 2f,
                top + cellSize / 2f + paint.getTextSize() / 3,
                paint);
    }

    private void drawAnimatedCell(Canvas canvas, int value, float left, float top, int cellSize) {
        paint.setColor(ContextCompat.getColor(getContext(), R.color.bg_block));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);

        if (value == 0) return;

        int index = (int) (Math.log(value) / Math.log(2)) - 1;
        int colorIndex = index % colorResList.length;

        paint.setColor(ContextCompat.getColor(getContext(), colorResList[colorIndex]));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(value),
                left + cellSize / 2f,
                top + cellSize / 2f + paint.getTextSize() / 3,
                paint);
    }

    private boolean isCellInAnimation(int row, int col) {
        for (AnimatedCell animCell : animatedCells) {
            if (animCell.toX == row && animCell.toY == col) return true;
        }
        return false;
    }

    /**
     * 动画入口：执行一次移动 + 播放
     */
    public void playMoveAnimation(List<AnimatedCell> animations) {
        if (animations.isEmpty()) return;

        isAnimating = true;
        this.animatedCells.clear();
        this.animatedCells.addAll(animations);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(100); // 🎯 更快的动画
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            for (AnimatedCell ac : animatedCells) {
                ac.animX = ac.fromY + (ac.toY - ac.fromY) * progress;
                ac.animY = ac.fromX + (ac.toX - ac.fromX) * progress;
            }
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                animatedCells.clear();
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return gestureDetector.onTouchEvent(event);
    }
}