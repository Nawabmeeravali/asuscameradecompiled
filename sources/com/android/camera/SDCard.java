package com.android.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import java.io.File;

public class SDCard {
    private static final boolean DEFAULT_SDCARD_PATH_DIRECTORY = SystemProperties.getBoolean("persist.sys.default.directory", true);
    private static final String TAG = "SDCard";
    private static final int VOLUME_SDCARD_INDEX = 1;
    private static SDCard sSDCard;
    private Context mContext;
    private BroadcastReceiver mMediaBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SDCard.this.initVolume();
        }
    };
    private String mPath = null;
    private String mRawpath = null;
    private StorageManager mStorageManager = null;
    private StorageVolume mVolume = null;

    public boolean isWriteable() {
        if (this.mVolume == null) {
            return false;
        }
        if ("mounted".equals(getSDCardStorageState())) {
            return true;
        }
        return false;
    }

    public String getDirectory() {
        if (this.mVolume == null) {
            return null;
        }
        if (this.mPath == null) {
            if (DEFAULT_SDCARD_PATH_DIRECTORY) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.mVolume.getPath());
                sb.append("/DCIM/Camera");
                this.mPath = sb.toString();
            } else {
                File[] externalFilesDirs = this.mContext.getExternalFilesDirs(null);
                if (externalFilesDirs != null) {
                    int i = 0;
                    while (true) {
                        if (i >= externalFilesDirs.length) {
                            break;
                        }
                        if (externalFilesDirs[i] != null) {
                            String absolutePath = externalFilesDirs[i].getAbsolutePath();
                            if (absolutePath.startsWith(this.mVolume.getPath())) {
                                this.mPath = absolutePath;
                                break;
                            }
                        }
                        i++;
                    }
                }
            }
        }
        return this.mPath;
    }

    public String getRawDirectory() {
        if (this.mVolume == null) {
            return null;
        }
        if (this.mRawpath == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mVolume.getPath());
            sb.append("/DCIM/Camera/raw");
            this.mRawpath = sb.toString();
        }
        return this.mRawpath;
    }

    public static void initialize(Context context) {
        if (sSDCard == null) {
            sSDCard = new SDCard(context);
        }
    }

    public static synchronized SDCard instance() {
        SDCard sDCard;
        synchronized (SDCard.class) {
            sDCard = sSDCard;
        }
        return sDCard;
    }

    private String getSDCardStorageState() {
        return this.mVolume.getState();
    }

    private SDCard(Context context) {
        try {
            this.mContext = context;
            this.mStorageManager = (StorageManager) context.getSystemService("storage");
            initVolume();
            registerMediaBroadcastreceiver(context);
        } catch (Exception e) {
            Log.e(TAG, "couldn't talk to MountService", e);
        }
    }

    /* access modifiers changed from: private */
    public void initVolume() {
        StorageVolume[] volumeList = this.mStorageManager.getVolumeList();
        this.mVolume = volumeList.length > 1 ? volumeList[1] : null;
        this.mPath = null;
        this.mRawpath = null;
    }

    private void registerMediaBroadcastreceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addDataScheme("file");
        context.registerReceiver(this.mMediaBroadcastReceiver, intentFilter);
    }
}
