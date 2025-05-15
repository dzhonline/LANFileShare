package com.dzh.game_2048;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dzh.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class Game2048Activity extends AppCompatActivity {

    private Game2048View gameView;
    private Game2048Model model;
    private SharedPreferences prefs;
    private boolean hasShown2048Dialog = false;
    private int animationDuration = 120; // 动画默认值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        gameView = findViewById(R.id.gameView);
        Button btnRestart = findViewById(R.id.btnRestart);
        model = gameView.getModel();
        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        prefs = getSharedPreferences("game_2048_data", Context.MODE_PRIVATE);

        loadProgress();

        btnRestart.setOnClickListener(v -> showRestartConfirm());

        gameView.setSwipeCallback(this::onSwipe);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgress();
    }

    private void showRestartConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("确认重新开始？")
                .setMessage("这将清除当前游戏进度")
                .setPositiveButton("重新开始", (dialog, which) -> {
                    model.reset();
                    gameView.invalidate();
                    model.reached2048 = false;
                    hasShown2048Dialog = false;
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSettingsDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_settings, null);
        SeekBar seekBar = view.findViewById(R.id.seekAnimationSpeed);
        TextView tvVal = view.findViewById(R.id.tvAnimationValue);

        seekBar.setProgress(animationDuration);
        tvVal.setText("当前：" + animationDuration + "ms");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVal.setText("当前：" + progress + "ms");
                animationDuration = progress;
                gameView.setAnimationDuration(animationDuration);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void saveProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        int size = model.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                editor.putInt("cell_" + i + "_" + j, model.grid[i][j]);
            }
        }
        editor.apply();
    }

    private void loadProgress() {
        int size = model.getSize();
        boolean hasData = false;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int val = prefs.getInt("cell_" + i + "_" + j, 0);
                if (val != 0) hasData = true;
                model.grid[i][j] = val;
            }
        }

        if (!hasData) {
            model.spawn();
            model.spawn();
        }

        gameView.invalidate();
    }

    private void onSwipe(String direction) {
        if ("done".equals(direction)) {
            model.spawn();
            gameView.invalidate();
            check2048Reached();
            return;
        }

        if (model.move(direction)) {
            List<AnimatedCell> anims = model.getLatestAnimations();
            gameView.playMoveAnimation(anims);
        }
    }

    private void check2048Reached() {
        if (!hasShown2048Dialog && model.hasReached2048()) {
            hasShown2048Dialog = true;
            new AlertDialog.Builder(this)
                    .setTitle("🎉 恭喜达成 2048！")
                    .setMessage("是否重新开始，还是继续挑战更高分数？")
                    .setPositiveButton("重新开始", (dialog, which) -> {
                        model.reset();
                        gameView.invalidate();
                        model.reached2048 = false;
                        hasShown2048Dialog = false;
                    })
                    .setNegativeButton("继续游戏", null)
                    .show();
        }
    }
}