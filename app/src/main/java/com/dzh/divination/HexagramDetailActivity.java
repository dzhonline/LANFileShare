package com.dzh.divination;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dzh.R;

import org.json.JSONObject;

import java.io.InputStream;

public class HexagramDetailActivity extends AppCompatActivity {

    private TextView nameText, chartText, analysisText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hexagram_detail);

        nameText = findViewById(R.id.textHexName);
        chartText = findViewById(R.id.textHexChart);
        analysisText = findViewById(R.id.textHexAnalysis);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        String hexKey = getIntent().getStringExtra("hexagramKey");

        if (hexKey != null) {
            displayHexagram(hexKey);
        } else {
            nameText.setText("没有对应卦码！");
        }
    }

    private void displayHexagram(String key) {
        try {
            JSONObject json = loadHexagramJson();
            JSONObject hex = json.getJSONObject(key);
            String name = hex.getString("name");
            String analysis = hex.getString("analysis");

            nameText.setText(name);

            StringBuilder chart = new StringBuilder();
            for (int i = 5; i >= 0; i--) {
                chart.append(key.charAt(i) == '1' ? "——" : "— —").append("\n");
            }
            chartText.setText(chart.toString());
            analysisText.setText(analysis);

        } catch (Exception e) {
            nameText.setText("读取卦象失败");
            e.printStackTrace();
        }
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