package com.dzh.lanfileshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class HttpService extends Service {
    private static final String CHANNEL_ID = "HTTP_SERVER";

    private MyHttpServer server;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "LAN 文件共享服务", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LAN 文件共享正在运行")
                .setContentText("可通过局域网访问共享文件")
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .build();

        startForeground(1, notification);

        try {
            server = new MyHttpServer(8080);
            server.start();
            LogHelper.addLog("✅ 启动后台 HTTP 服务");
        } catch (Exception e) {
            LogHelper.addLog("❌ 启动服务失败：" + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            LogHelper.addLog("ℹ️ 服务关闭（后台）");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}