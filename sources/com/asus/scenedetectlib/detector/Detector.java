package com.asus.scenedetectlib.detector;

import android.content.Context;
import android.graphics.Bitmap;
import com.asus.scenedetectlib.AISceneDetectInterface.recognition.Recognition;
import java.util.List;

public abstract class Detector {
    public Detector(Context context) {
    }

    /* access modifiers changed from: protected */
    /* renamed from: a */
    public abstract List<Recognition> mo8225a(Bitmap bitmap);

    public List<? extends Recognition> recognizeImage(Bitmap bitmap) {
        return mo8225a(bitmap);
    }

    public void release() {
    }
}
