package com.dzh.divination;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dzh.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DivinationActivity extends AppCompatActivity {

    private ListView recordListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<HexRecord> recordList;
    private ArrayList<String> recordDisplayList;

    private SharedPreferences prefs;
    private static final String PREF_KEY = "hex_records";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_divination);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("卜卦记录");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recordListView = findViewById(R.id.listViewRecords);
        Button startButton = findViewById(R.id.btnStartDivination);

        prefs = getSharedPreferences("divination", MODE_PRIVATE);
        recordList = loadRecords();
        recordDisplayList = buildDisplayList();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recordDisplayList);
        recordListView.setAdapter(adapter);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TossCoinActivity.class);
            startActivityForResult(intent, 1001);
        });

        recordListView.setOnItemClickListener((parent, view, position, id) -> {
            HexRecord record = recordList.get(position);
            Intent intent = new Intent(this, HexagramDetailActivity.class);
            intent.putExtra("hexagramKey", record.key);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("hexagramName");
            String key = data.getStringExtra("hexagramKey");
            String time = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.CHINESE).format(new Date());

            HexRecord newRecord = new HexRecord(time, name, key);
            recordList.add(0, newRecord);
            saveRecords(recordList);

            recordDisplayList.add(0, newRecord.toString());
            adapter.notifyDataSetChanged();
        }
    }

    private void saveRecords(ArrayList<HexRecord> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString(PREF_KEY, json).apply();
    }

    private ArrayList<HexRecord> loadRecords() {
        Gson gson = new Gson();
        String json = prefs.getString(PREF_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<HexRecord>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<String> buildDisplayList() {
        ArrayList<String> display = new ArrayList<>();
        for (HexRecord record : recordList) {
            display.add(record.toString());
        }
        return display;
    }
}