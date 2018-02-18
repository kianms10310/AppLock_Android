package com.example.a8_716_05.myapplication;

import android.graphics.drawable.Drawable;

/**
 * Created by Shin on 2016-12-03.
 */

public class AppListItem {
    private Drawable icon;
    private String packName;
    private String appName;
    private boolean enable;

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public boolean getEnable() {
        return enable;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getPackName() {
        return packName;
    }

    public String getAppName() {
        return appName;
    }


    public AppListItem(Drawable icon, String packName, String appName, boolean en){
        this.icon = icon;
        this.packName = packName;
        this.appName = appName;
        this.enable = en;
    }
}
