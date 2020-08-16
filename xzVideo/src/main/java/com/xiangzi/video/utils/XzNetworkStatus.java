package com.xiangzi.video.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class XzNetworkStatus {

    public static final int CONNECTED_WIFI = 0;
    public static final int CONNECTED_4G = 1;
    public static final int NOT_CONNECTED = 2;

    @IntDef({CONNECTED_WIFI,CONNECTED_4G,NOT_CONNECTED})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NetworkStatus{}

}
