package com.android.camera.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.net.Uri;
import com.android.camera.ImageTaskManager;
import com.android.camera.ImageTaskManager.TaskListener;
import com.android.camera.Storage;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class PlaceholderManager implements ImageTaskManager {
    public static final String PLACEHOLDER_MIME_TYPE = "application/placeholder-image";
    private static final String TAG = "PlaceholderManager";
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ArrayList<WeakReference<TaskListener>> mListenerRefs = new ArrayList<>();

    private class ListenerIterator implements Iterator<TaskListener> {
        private int mIndex;
        private TaskListener mNext;

        private ListenerIterator() {
            this.mIndex = 0;
            this.mNext = null;
        }

        public boolean hasNext() {
            while (this.mNext == null && this.mIndex < PlaceholderManager.this.mListenerRefs.size()) {
                this.mNext = (TaskListener) ((WeakReference) PlaceholderManager.this.mListenerRefs.get(this.mIndex)).get();
                if (this.mNext == null) {
                    PlaceholderManager.this.mListenerRefs.remove(this.mIndex);
                }
            }
            return this.mNext != null;
        }

        public TaskListener next() {
            hasNext();
            this.mIndex++;
            TaskListener taskListener = this.mNext;
            this.mNext = null;
            return taskListener;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Session {
        String outputTitle;
        Uri outputUri;
        long time;

        Session(String str, Uri uri, long j) {
            this.outputTitle = str;
            this.outputUri = uri;
            this.time = j;
        }
    }

    public int getTaskProgress(Uri uri) {
        return 0;
    }

    public PlaceholderManager(Context context) {
        this.mContext = context;
    }

    public void addTaskListener(TaskListener taskListener) {
        synchronized (this.mListenerRefs) {
            if (findTaskListener(taskListener) == -1) {
                this.mListenerRefs.add(new WeakReference(taskListener));
            }
        }
    }

    public void removeTaskListener(TaskListener taskListener) {
        synchronized (this.mListenerRefs) {
            int findTaskListener = findTaskListener(taskListener);
            if (findTaskListener != -1) {
                this.mListenerRefs.remove(findTaskListener);
            }
        }
    }

    private int findTaskListener(TaskListener taskListener) {
        for (int i = 0; i < this.mListenerRefs.size(); i++) {
            TaskListener taskListener2 = (TaskListener) ((WeakReference) this.mListenerRefs.get(i)).get();
            if (taskListener2 != null && taskListener2 == taskListener) {
                return i;
            }
        }
        return -1;
    }

    private Iterable<TaskListener> getListeners() {
        return new Iterable<TaskListener>() {
            public Iterator<TaskListener> iterator() {
                return new ListenerIterator();
            }
        };
    }

    public Session insertPlaceholder(String str, byte[] bArr, long j) {
        byte[] bArr2 = bArr;
        if (str == null || bArr2 == null) {
            throw new IllegalArgumentException("Null argument passed to insertPlaceholder");
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bArr, 0, bArr2.length, options);
        int i = options.outWidth;
        int i2 = options.outHeight;
        if (i <= 0 || i2 <= 0) {
            throw new IllegalArgumentException("Image had bad height/width");
        }
        Uri addImage = Storage.addImage(this.mContext.getContentResolver(), str, j, (Location) null, 0, (ExifInterface) null, bArr, i, i2, PLACEHOLDER_MIME_TYPE);
        if (addImage == null) {
            return null;
        }
        String path = addImage.getPath();
        synchronized (this.mListenerRefs) {
            for (TaskListener onTaskQueued : getListeners()) {
                onTaskQueued.onTaskQueued(path, addImage);
            }
        }
        return new Session(str, addImage, j);
    }

    public void replacePlaceholder(Session session, Location location, int i, ExifInterface exifInterface, byte[] bArr, int i2, int i3, String str) {
        Session session2 = session;
        Storage.updateImage(session2.outputUri, this.mContext.getContentResolver(), session2.outputTitle, session2.time, location, i, exifInterface, bArr, i2, i3, str);
        synchronized (this.mListenerRefs) {
            for (TaskListener onTaskDone : getListeners()) {
                onTaskDone.onTaskDone(session2.outputUri.getPath(), session2.outputUri);
            }
        }
        CameraUtil.broadcastNewPicture(this.mContext, session2.outputUri);
    }

    public void removePlaceholder(Session session) {
        Storage.deleteImage(this.mContext.getContentResolver(), session.outputUri);
    }
}
