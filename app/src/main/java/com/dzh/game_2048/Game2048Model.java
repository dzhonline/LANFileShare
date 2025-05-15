package com.dzh.game_2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Model {
    public int[][] grid;
    private final int size;
    private final Random random = new Random();
    public boolean reached2048 = false;

    // ⏺️ 每一次合并/移动路径保存在这里
    private final List<AnimatedCell> latestAnimations = new ArrayList<>();

    public Game2048Model(int size) {
        this.size = size;
        grid = new int[size][size];
        spawn();
        spawn();
    }

    public List<AnimatedCell> getLatestAnimations() {
        return latestAnimations;
    }

    public int getSize() {
        return size;
    }

    public boolean hasReached2048() {
        return reached2048;
    }

    public boolean move(String dir) {
        boolean moved = false;
        latestAnimations.clear();

        int[][] newGrid = new int[size][size];

        for (int i = 0; i < size; i++) {
            int[] line = new int[size];
            for (int j = 0; j < size; j++) {
                switch (dir) {
                    case "up":    line[j] = grid[j][i]; break;
                    case "down":  line[j] = grid[size - 1 - j][i]; break;
                    case "left":  line[j] = grid[i][j]; break;
                    case "right": line[j] = grid[i][size - 1 - j]; break;
                }
            }

            int[] merged = new int[size];
            int index = 0;
            boolean[] mergedFlags = new boolean[size];

            for (int j = 0; j < size; j++) {
                if (line[j] == 0) continue;

                if (index > 0 && merged[index - 1] == line[j] && !mergedFlags[index - 1]) {
                    merged[index - 1] *= 2;
                    mergedFlags[index - 1] = true;

                    if (merged[index - 1] == 2048 && !reached2048) {
                        reached2048 = true;
                    }

                    // 记录动画路径
                    addAnim(i, j, index - 1, dir, merged[index - 1], true);
                    moved = true;
                } else {
                    merged[index] = line[j];

                    if (index != j) { // 如果位置变了，说明发生了移动
                        addAnim(i, j, index, dir, merged[index], false);
                        moved = true;
                    }

                    index++;
                }
            }

            for (int j = 0; j < size; j++) {
                int value = merged[j];
                switch (dir) {
                    case "up":       newGrid[j][i] = value; break;
                    case "down":     newGrid[size - 1 - j][i] = value; break;
                    case "left":     newGrid[i][j] = value; break;
                    case "right":    newGrid[i][size - 1 - j] = value; break;
                }
            }
        }

        if (moved) grid = newGrid;

        return moved;
    }

    /** 动画记录生成 */
    private void addAnim(int i, int j, int newPosIndex, String dir, int value, boolean merging) {
        int fromX = i;
        int fromY = j;
        int toX = i;
        int toY = j;

        switch (dir) {
            case "up":
                fromX = j;
                fromY = i;
                toX = newPosIndex;
                toY = i;
                break;
            case "down":
                fromX = size - 1 - j;
                fromY = i;
                toX = size - 1 - newPosIndex;
                toY = i;
                break;
            case "left":
                fromX = i;
                fromY = j;
                toX = i;
                toY = newPosIndex;
                break;
            case "right":
                fromX = i;
                fromY = size - 1 - j;
                toX = i;
                toY = size - 1 - newPosIndex;
                break;
        }

        latestAnimations.add(new AnimatedCell(fromX, fromY, toX, toY, value, merging));
    }

    public void spawn() {
        int x, y;
        do {
            x = random.nextInt(size);
            y = random.nextInt(size);
        } while (grid[x][y] != 0);
        grid[x][y] = random.nextDouble() < 0.9 ? 2 : 4;
    }

    public void reset() {
        grid = new int[size][size];
        spawn();
        spawn();
        reached2048 = false;
    }
}