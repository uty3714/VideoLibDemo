package com.xiangzi.video.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.xiangzi.video.utils.XzAppGlobals;
import com.xiangzi.video.utils.XzNetworkStatus;
import com.xiangzi.video.view.XzVideoPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 网络状态监听广播
 */
public class XzNetworkChangeReceiver extends BroadcastReceiver {

    private XzNetworkChangeListener mListener;
    public AtomicBoolean mAtomicBoolean = null;

    public XzNetworkChangeReceiver(XzNetworkChangeListener mListener) {
        this.mListener = mListener;
        this.mAtomicBoolean = new AtomicBoolean(false);
    }

    public void registerNetworkChangeBroadcast() {
        if (mAtomicBoolean.get()) {
            return;
        }
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            XzAppGlobals.get().registerReceiver(this, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterNetworkChangeBroadcast() {
        if (!mAtomicBoolean.get()) {
            return;
        }
        XzAppGlobals.get().unregisterReceiver(this);
        mAtomicBoolean.set(false);
    }

    public static boolean isWifi() {
        return getNetworkStatus() == XzNetworkStatus.CONNECTED_WIFI;
    }

    public static boolean is4G() {
        return getNetworkStatus() == XzNetworkStatus.CONNECTED_4G;
    }

    public static boolean isDisconnect() {
        return getNetworkStatus() == XzNetworkStatus.NOT_CONNECTED;
    }

    public static int getNetworkStatus() {
        int networkStatus = XzNetworkStatus.CONNECTED_WIFI;
        ConnectivityManager connectivityManager = (ConnectivityManager) XzAppGlobals.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                if (TextUtils.equals(name, "WIFI")) {
                    networkStatus = XzNetworkStatus.CONNECTED_WIFI;
                } else {
                    networkStatus = XzNetworkStatus.CONNECTED_4G;
                }
            } else {
                networkStatus = XzNetworkStatus.NOT_CONNECTED;
            }
        }
        return networkStatus;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (mListener != null) {
                mListener.onNetworkChange(getNetworkStatus());
            }
        }
    }


    public interface XzNetworkChangeListener {
        void onNetworkChange(@XzNetworkStatus.NetworkStatus int networkStatus);
    }

}
