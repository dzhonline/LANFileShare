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
    private int animationDuration = 120;
    public void setAnimationDuration(int duration) {
        this.animationDuration = Math.max(duration, 50); // 保底，防止设置为0
    }

    private final List<AnimatedCell> animatedCells = new ArrayList<>();
    private boolean isAnimating = false;
    private float animProgress = 1f;

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

        // 背景格子（背景必须统一绘制）
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                drawCellBackground(canvas, i, j, cellSize);
            }
        }

        // 静态格子（排除参与动画的目标格子）
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int value = model.grid[i][j];

                if (isAnimating && isCellTarget(i, j)) {
                    continue; // 动画中由 AnimatedCell 控制
                }

                drawCellStatic(canvas, i, j, value, cellSize);
            }
        }

        // 动画格子绘制（主动画控制流程）
        if (isAnimating) {
            for (AnimatedCell cell : animatedCells) {
                drawCellAnimated(canvas, cell, cellSize);
            }
        }
    }

    private void drawCellBackground(Canvas canvas, int i, int j, int cellSize) {
        float left = j * cellSize;
        float top = i * cellSize;
        paint.setColor(ContextCompat.getColor(getContext(), R.color.bg_block));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);
    }

    private void drawCellStatic(Canvas canvas, int i, int j, int value, int cellSize) {
        if (value == 0) return;
        float left = j * cellSize;
        float top = i * cellSize;

        paint.setColor(getColorForValue(value));
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);
        drawCenteredText(canvas, String.valueOf(value), left, top, cellSize);
    }

    private void drawCellAnimated(Canvas canvas, AnimatedCell cell, int cellSize) {
        float startX = cell.fromY * cellSize;
        float startY = cell.fromX * cellSize;
        float endX = cell.toY * cellSize;
        float endY = cell.toX * cellSize;

        float currentX = startX + (endX - startX) * animProgress;
        float currentY = startY + (endY - startY) * animProgress;

        paint.setColor(interpolateColor(getColorForValue(cell.fromValue), getColorForValue(cell.toValue)));
        canvas.drawRect(currentX, currentY, currentX + cellSize, currentY + cellSize, paint);

        if (animProgress <= 0.5f) {
            // 前数字淡出阶段
            int fromAlpha = (int) ((1f - animProgress * 2) * 255); // 1 -> 0
            paint.setAlpha(fromAlpha);
            drawCenteredText(canvas, String.valueOf(cell.fromValue), currentX, currentY, cellSize);
        } else {
            // 后数字淡入阶段
            int toAlpha = (int) ((animProgress - 0.5f) * 2 * 255); // 0 -> 1
            paint.setAlpha(toAlpha);
            drawCenteredText(canvas, String.valueOf(cell.toValue), currentX, currentY, cellSize);
        }

        paint.setAlpha(255); // 重置透明度
    }

    private void drawCenteredText(Canvas canvas, String text, float left, float top, int size) {
        paint.setColor(Color.BLACK);
        canvas.drawText(text,
                left + size / 2f,
                top + size / 2f + paint.getTextSize() / 3f,
                paint);
    }

    private boolean isCellTarget(int row, int col) {
        for (AnimatedCell c : animatedCells) {
            if (c.toX == row && c.toY == col) return true;
        }
        return false;
    }

    private int getColorForValue(int value) {
        if (value == 0) return Color.LTGRAY;
        int index = (int) (Math.log(value) / Math.log(2)) - 1;
        index = index % colorResList.length;
        return ContextCompat.getColor(getContext(), colorResList[index]);
    }

    private int interpolateColor(int fromColor, int toColor) {
        int r1 = Color.red(fromColor);
        int g1 = Color.green(fromColor);
        int b1 = Color.blue(fromColor);
        int r2 = Color.red(toColor);
        int g2 = Color.green(toColor);
        int b2 = Color.blue(toColor);

        int r = (int) (r1 + (r2 - r1) * animProgress);
        int g = (int) (g1 + (g2 - g1) * animProgress);
        int b = (int) (b1 + (b2 - b1) * animProgress);

        return Color.rgb(r, g, b);
    }

    public void playMoveAnimation(List<AnimatedCell> animations) {
        if (animations.isEmpty()) return;

        isAnimating = true;
        animatedCells.clear();
        animatedCells.addAll(animations);
        animProgress = 0f;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                animatedCells.clear();

                // ✅ 动画完成后再请求 spawn 刷新（刷新数据层）
                invalidate();

                if (externalCallback != null) {
                    // 通知 activity 做 spawn、check 状态等
                    externalCallback.onSwipe("done"); // 你可扩展一个 "done" 标记
                }
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