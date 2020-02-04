package com.android.camera;

import android.net.Uri;

public interface ImageTaskManager {

    public interface TaskListener {
        void onTaskDone(String str, Uri uri);

        void onTaskProgress(String str, Uri uri, int i);

        void onTaskQueued(String str, Uri uri);
    }

    void addTaskListener(TaskListener taskListener);

    int getTaskProgress(Uri uri);

    void removeTaskListener(TaskListener taskListener);
}
