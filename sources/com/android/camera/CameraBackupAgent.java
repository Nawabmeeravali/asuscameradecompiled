package com.android.camera;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;

public class CameraBackupAgent extends BackupAgentHelper {
    private static final String CAMERA_BACKUP_KEY = "camera_prefs";

    public void onCreate() {
        Context applicationContext = getApplicationContext();
        addHelper(CAMERA_BACKUP_KEY, new SharedPreferencesBackupHelper(applicationContext, ComboPreferences.getSharedPreferencesNames(applicationContext)));
    }
}
