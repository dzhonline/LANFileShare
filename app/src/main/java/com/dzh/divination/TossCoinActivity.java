package com.dzh.divination;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dzh.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TossCoinActivity extends Activity {

    private FrameLayout board;
    private final StringBuilder hexLineBuilder = new StringBuilder();
    private TextView progressText, hexLineText, hexChartText;
    private Button btnToss;

    private int lineIndex = 0;
    private final List<Integer> lines = new ArrayList<>();
    private final Handler handler = new Handler();

    private Drawable coinFront, coinBack;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toss_coin);

        board = findViewById(R.id.board);
        progressText = findViewById(R.id.textProgress);
        btnToss = findViewById(R.id.btnToss);
        hexLineText = findViewById(R.id.textHexLines);
        hexChartText = findViewById(R.id.textHexPicture);

        coinFront = getDrawable(R.drawable.coin_front);
        coinBack = getDrawable(R.drawable.coin_back);

        updateProgress();
        btnToss.setOnClickListener(v -> tossCoins());
    }

    private void updateProgress() {
        progressText.setText("第 " + (lineIndex + 1) + " 次投掷");
        btnToss.setText("投掷硬币 (" + (lineIndex + 1) + "/6)");
    }

    private String lineToSymbol(int sum) {
        if (sum == 6 || sum == 8) return "— —";  // 阴爻
        else return "——";  // 阳爻
    }

    private void tossCoins() {
        btnToss.setEnabled(false);
        board.removeAllViews();
        int sum = 0;

        for (int i = 0; i < 3; i++) {
            boolean isHead = random.nextBoolean();
            Drawable face = isHead ? coinFront : coinBack;
            sum += isHead ? 3 : 2;

            final ImageView coin = new ImageView(this);
            coin.setImageDrawable(face);

            // 初始位置在屏幕上方中间
            int size = 150;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            board.addView(coin, params);

            int boardWidth = board.getWidth();
            int boardHeight = board.getHeight();
            int startX = boardWidth / 2;
            int startY = -200;

            int endX = random.nextInt(boardWidth - size);
            int endY = random.nextInt(boardHeight - size);

            Path path = new Path();
            path.moveTo(startX, startY);
            path.quadTo(startX + (endX - startX) / 2f, startY + 100, endX, endY);

            ObjectAnimator fall = ObjectAnimator.ofFloat(coin, View.X, View.Y, path);
            fall.setDuration(1000);

            ObjectAnimator rotate = ObjectAnimator.ofFloat(coin, "rotation", 0f, 360f * (2 + random.nextInt(2)));
            rotate.setDuration(1000);

            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(fall, rotate);
            animSet.start();
        }

        int finalSum = sum;

        handler.postDelayed(() -> {
            lines.add(finalSum);
            lineIndex++;

            String symbol = lineToSymbol(finalSum);
            hexLineBuilder.append("第 ").append(lineIndex).append(" 爻: ").append(symbol).append("\n");

            runOnUiThread(() -> {
                hexLineText.setText("当前爻象：\n" + hexLineBuilder.toString());

                StringBuilder chart = new StringBuilder("六爻结构（从下往上）：\n");
                for (int i = lines.size() - 1; i >= 0; i--) {
                    chart.append(lineToSymbol(lines.get(i))).append("\n");
                }
                hexChartText.setText(chart.toString());
            });

            // 最后一次点击后修改按钮行为
            if (lineIndex >= 6) {
                runOnUiThread(() -> {
                    btnToss.setText("查看卦象解读");
                    btnToss.setOnClickListener(v -> showResultDialog());
                    btnToss.setEnabled(true);
                });
            } else {
                updateProgress();
                btnToss.setEnabled(true);
            }

        }, 1200);
    }

    private void showResultDialog() {
        try {
            String key = buildHexKey(lines);
            JSONObject json = loadHexagramJson();
            JSONObject thisHex = json.getJSONObject(key);

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_result, null);
            TextView name = dialogView.findViewById(R.id.hexagramName);
            TextView desc = dialogView.findViewById(R.id.hexagramAnalysis);

            name.setText(thisHex.getString("name"));
            desc.setText(thisHex.getString("analysis"));

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
                dialog.dismiss();
                lineIndex = 0;
                lines.clear();
                hexLineBuilder.setLength(0);
                hexLineText.setText("当前爻象：");
                hexChartText.setText("六爻结构：");
                updateProgress();
                btnToss.setEnabled(true);
                btnToss.setOnClickListener(x -> tossCoins());
            });

            dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
                Intent data = new Intent();
                data.putExtra("hexagramName", thisHex.optString("name"));
                data.putExtra("hexagramKey", buildHexKey(lines)); // 🔥 加这一行
                setResult(RESULT_OK, data);
                dialog.dismiss();
                finish();
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildHexKey(List<Integer> lines) {
        StringBuilder key = new StringBuilder();
        for (int i = 5; i >= 0; i--) {
            int v = lines.get(i);
            key.append((v == 7 || v == 9) ? "1" : "0");
        }
        return key.toString();
    }

    private JSONObject loadHexagramJson() throws Exception {
        InputStream is = getAssets().open("hexagrams.json");
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        String json = new String(buffer);
        return new JSONObject(json);
    }
}