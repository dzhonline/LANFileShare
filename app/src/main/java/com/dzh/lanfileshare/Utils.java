package com.dzh.lanfileshare;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class Utils {
    public static String getIPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}