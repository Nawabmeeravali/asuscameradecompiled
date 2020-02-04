package com.android.camera.app;

import android.content.Context;
import android.net.Uri;
import com.android.camera.ImageTaskManager;
import com.android.camera.ImageTaskManager.TaskListener;

public class PanoramaStitchingManager implements ImageTaskManager {
    public void addTaskListener(TaskListener taskListener) {
    }

    public int getTaskProgress(Uri uri) {
        return -1;
    }

    public void removeTaskListener(TaskListener taskListener) {
    }

    public PanoramaStitchingManager(Context context) {
    }
}
