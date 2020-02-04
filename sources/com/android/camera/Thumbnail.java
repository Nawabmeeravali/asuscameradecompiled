package com.android.camera;

import android.graphics.Bitmap;
import java.io.FileDescriptor;

public class Thumbnail {
    public static Bitmap createVideoThumbnailBitmap(FileDescriptor fileDescriptor, int i) {
        return createVideoThumbnailBitmap(null, fileDescriptor, i);
    }

    public static Bitmap createVideoThumbnailBitmap(String str, int i) {
        return createVideoThumbnailBitmap(str, null, i);
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x001e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.graphics.Bitmap createVideoThumbnailBitmap(java.lang.String r2, java.io.FileDescriptor r3, int r4) {
        /*
            android.media.MediaMetadataRetriever r0 = new android.media.MediaMetadataRetriever
            r0.<init>()
            r1 = 0
            if (r2 == 0) goto L_0x000c
            r0.setDataSource(r2)     // Catch:{ IllegalArgumentException | RuntimeException -> 0x001e, all -> 0x0019 }
            goto L_0x000f
        L_0x000c:
            r0.setDataSource(r3)     // Catch:{ IllegalArgumentException | RuntimeException -> 0x001e, all -> 0x0019 }
        L_0x000f:
            r2 = -1
            android.graphics.Bitmap r2 = r0.getFrameAtTime(r2)     // Catch:{ IllegalArgumentException | RuntimeException -> 0x001e, all -> 0x0019 }
            r0.release()     // Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0022
        L_0x0019:
            r2 = move-exception
            r0.release()     // Catch:{ RuntimeException -> 0x001d }
        L_0x001d:
            throw r2
        L_0x001e:
            r0.release()     // Catch:{ RuntimeException -> 0x0021 }
        L_0x0021:
            r2 = r1
        L_0x0022:
            if (r2 != 0) goto L_0x0025
            return r1
        L_0x0025:
            int r3 = r2.getWidth()
            int r0 = r2.getHeight()
            if (r3 <= r4) goto L_0x0042
            float r4 = (float) r4
            float r3 = (float) r3
            float r4 = r4 / r3
            float r3 = r3 * r4
            int r3 = java.lang.Math.round(r3)
            float r0 = (float) r0
            float r4 = r4 * r0
            int r4 = java.lang.Math.round(r4)
            r0 = 1
            android.graphics.Bitmap r2 = android.graphics.Bitmap.createScaledBitmap(r2, r3, r4, r0)
        L_0x0042:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.Thumbnail.createVideoThumbnailBitmap(java.lang.String, java.io.FileDescriptor, int):android.graphics.Bitmap");
    }
}
