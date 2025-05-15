package com.dzh.lanfileshare;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.dzh.R;

import java.util.List;

public class LogActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        listView = findViewById(R.id.listView);
        btnClear = findViewById(R.id.btnClear);

        btnClear.setOnClickListener(v -> {
            LogHelper.clearLogs();
            updateList();
        });

        updateList();
    }

    private void updateList() {
        List<String> logs = LogHelper.getLogs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, logs);
        listView.setAdapter(adapter);
    }
}