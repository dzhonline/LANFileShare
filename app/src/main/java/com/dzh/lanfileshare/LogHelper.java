package com.dzh.lanfileshare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogHelper {
    private static final List<String> logList = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void addLog(String message) {
        String time = sdf.format(new Date());
        logList.add("[" + time + "] " + message);
    }

    public static List<String> getLogs() {
        return new ArrayList<>(logList);
    }

    public static void clearLogs() {
        logList.clear();
    }
}