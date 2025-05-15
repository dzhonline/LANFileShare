package com.dzh;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzh.game_2048.Game2048Activity;
import com.dzh.lanfileshare.FunctionAdapter;
import com.dzh.lanfileshare.FunctionItem;
import com.dzh.lanfileshare.LanFileShareActivity;
import com.dzh.lanfileshare.LogActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        recyclerView = findViewById(R.id.rvFunctions);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_logs) {
                startActivity(new Intent(this, LogActivity.class));
            }
            drawer.closeDrawers();
            return true;
        });

        // 功能列表
        List<FunctionItem> functions = Arrays.asList(
                new FunctionItem("📁 文件共享", R.mipmap.ic_launcher_foreground, LanFileShareActivity.class),
                new FunctionItem("🧾 日志查看", R.mipmap.ic_launcher_foreground, LogActivity.class),
                new FunctionItem("🎮 2048", R.mipmap.ic_launcher_foreground, Game2048Activity.class)
        );
        FunctionAdapter adapter = new FunctionAdapter(functions, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}