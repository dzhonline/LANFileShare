package com.dzh.lanfileshare;

public class FunctionItem {
    public String title;
    public int iconRes;
    public Class<?> targetActivity;

    public FunctionItem(String title, int iconRes, Class<?> targetActivity) {
        this.title = title;
        this.iconRes = iconRes;
        this.targetActivity = targetActivity;
    }
}