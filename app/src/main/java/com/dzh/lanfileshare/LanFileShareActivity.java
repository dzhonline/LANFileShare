package com.dzh.lanfileshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dzh.R;

import java.util.ArrayList;
import java.util.List;

public class LanFileShareActivity extends AppCompatActivity {

    private Button btnToggle;
    private TextView tvStatus;
    private TextView tvPermissionHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanfile); // 这个布局我们下一步创建

        btnToggle = findViewById(R.id.btnToggle);
        tvStatus = findViewById(R.id.tvStatus);
        tvPermissionHint = findViewById(R.id.tvPermissionHint);

        // 权限检测展示
        checkPermissions();
        checkAllFilePermission();

        btnToggle.setOnClickListener(v -> {
            if (btnToggle.getText().equals(getString(R.string.start_server))) {
                startServer();
            } else {
                stopServer();
            }
        });
    }

    private void startServer() {
        Intent intent = new Intent(this, HttpService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        tvStatus.setText("✅ 服务运行中：http://" + ip + ":8080");
        btnToggle.setText(R.string.stop_server);
    }

    private void stopServer() {
        Intent intent = new Intent(this, HttpService.class);
        stopService(intent);
        tvStatus.setText("服务已关闭");
        btnToggle.setText(R.string.start_server);
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
        };

        List<String> toRequest = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(perm);
            }
        }

        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    toRequest.toArray(new String[0]), 100);
        }
    }

    private void checkAllFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                tvPermissionHint.setVisibility(View.VISIBLE);
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "⚠️ 当前系统无法自动跳转文件管理权限，请手动授权", Toast.LENGTH_LONG).show();
                }
            } else {
                tvPermissionHint.setVisibility(View.GONE);
            }
        }
    }
}