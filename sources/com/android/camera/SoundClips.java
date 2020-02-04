package com.android.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import com.android.camera.util.ApiHelper;
import org.codeaurora.snapcam.C0905R;

public class SoundClips {
    public static final int FOCUS_COMPLETE = 0;
    public static final int SHUTTER_CLICK = 3;
    public static final int START_VIDEO_RECORDING = 1;
    public static final int STOP_VIDEO_RECORDING = 2;

    @TargetApi(16)
    private static class MediaActionSoundPlayer implements Player {
        private static final String TAG = "MediaActionSoundPlayer";
        private MediaActionSound mSound = new MediaActionSound();

        public void release() {
            MediaActionSound mediaActionSound = this.mSound;
            if (mediaActionSound != null) {
                mediaActionSound.release();
                this.mSound = null;
            }
        }

        public MediaActionSoundPlayer() {
            this.mSound.load(2);
            this.mSound.load(3);
            this.mSound.load(1);
            this.mSound.load(0);
        }

        public synchronized void play(int i) {
            if (i == 0) {
                this.mSound.play(1);
            } else if (i == 1) {
                this.mSound.play(2);
            } else if (i == 2) {
                this.mSound.play(3);
            } else if (i != 3) {
                String str = TAG;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unrecognized action:");
                    sb.append(i);
                    Log.w(str, sb.toString());
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mSound.play(0);
            }
        }
    }

    public interface Player {
        void play(int i);

        void release();
    }

    private static class SoundPoolPlayer implements Player, OnLoadCompleteListener {
        private static final int ID_NOT_LOADED = 0;
        private static final int NUM_SOUND_STREAMS = 1;
        private static final int[] SOUND_RES = {C0905R.raw.focus_complete, C0905R.raw.video_record};
        private static final String TAG = "SoundPoolPlayer";
        private Context mContext;
        private final boolean[] mSoundIDReady;
        private int mSoundIDToPlay;
        private final int[] mSoundIDs;
        private SoundPool mSoundPool;
        private final int[] mSoundRes = {0, 1, 1, 1};

        public SoundPoolPlayer(Context context) {
            this.mContext = context;
            this.mSoundIDToPlay = 0;
            this.mSoundPool = new SoundPool(1, SoundClips.getAudioTypeForSoundPool(), 0);
            this.mSoundPool.setOnLoadCompleteListener(this);
            int[] iArr = SOUND_RES;
            this.mSoundIDs = new int[iArr.length];
            this.mSoundIDReady = new boolean[iArr.length];
            int i = 0;
            while (true) {
                int[] iArr2 = SOUND_RES;
                if (i < iArr2.length) {
                    this.mSoundIDs[i] = this.mSoundPool.load(this.mContext, iArr2[i], 1);
                    this.mSoundIDReady[i] = false;
                    i++;
                } else {
                    return;
                }
            }
        }

        public synchronized void release() {
            if (this.mSoundPool != null) {
                this.mSoundPool.release();
                this.mSoundPool = null;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void play(int r8) {
            /*
                r7 = this;
                monitor-enter(r7)
                if (r8 < 0) goto L_0x004b
                int[] r0 = r7.mSoundRes     // Catch:{ all -> 0x0068 }
                int r0 = r0.length     // Catch:{ all -> 0x0068 }
                if (r8 < r0) goto L_0x0009
                goto L_0x004b
            L_0x0009:
                int[] r0 = r7.mSoundRes     // Catch:{ all -> 0x0068 }
                r8 = r0[r8]     // Catch:{ all -> 0x0068 }
                int[] r0 = r7.mSoundIDs     // Catch:{ all -> 0x0068 }
                r0 = r0[r8]     // Catch:{ all -> 0x0068 }
                if (r0 != 0) goto L_0x002b
                int[] r0 = r7.mSoundIDs     // Catch:{ all -> 0x0068 }
                android.media.SoundPool r1 = r7.mSoundPool     // Catch:{ all -> 0x0068 }
                android.content.Context r2 = r7.mContext     // Catch:{ all -> 0x0068 }
                int[] r3 = SOUND_RES     // Catch:{ all -> 0x0068 }
                r3 = r3[r8]     // Catch:{ all -> 0x0068 }
                r4 = 1
                int r1 = r1.load(r2, r3, r4)     // Catch:{ all -> 0x0068 }
                r0[r8] = r1     // Catch:{ all -> 0x0068 }
                int[] r0 = r7.mSoundIDs     // Catch:{ all -> 0x0068 }
                r8 = r0[r8]     // Catch:{ all -> 0x0068 }
                r7.mSoundIDToPlay = r8     // Catch:{ all -> 0x0068 }
                goto L_0x0049
            L_0x002b:
                boolean[] r0 = r7.mSoundIDReady     // Catch:{ all -> 0x0068 }
                boolean r0 = r0[r8]     // Catch:{ all -> 0x0068 }
                if (r0 != 0) goto L_0x0038
                int[] r0 = r7.mSoundIDs     // Catch:{ all -> 0x0068 }
                r8 = r0[r8]     // Catch:{ all -> 0x0068 }
                r7.mSoundIDToPlay = r8     // Catch:{ all -> 0x0068 }
                goto L_0x0049
            L_0x0038:
                android.media.SoundPool r0 = r7.mSoundPool     // Catch:{ all -> 0x0068 }
                int[] r1 = r7.mSoundIDs     // Catch:{ all -> 0x0068 }
                r1 = r1[r8]     // Catch:{ all -> 0x0068 }
                r2 = 1065353216(0x3f800000, float:1.0)
                r3 = 1065353216(0x3f800000, float:1.0)
                r4 = 0
                r5 = 0
                r6 = 1065353216(0x3f800000, float:1.0)
                r0.play(r1, r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0068 }
            L_0x0049:
                monitor-exit(r7)
                return
            L_0x004b:
                java.lang.String r0 = "SoundPoolPlayer"
                java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0068 }
                r1.<init>()     // Catch:{ all -> 0x0068 }
                java.lang.String r2 = "Resource ID not found for action:"
                r1.append(r2)     // Catch:{ all -> 0x0068 }
                r1.append(r8)     // Catch:{ all -> 0x0068 }
                java.lang.String r8 = " in play()."
                r1.append(r8)     // Catch:{ all -> 0x0068 }
                java.lang.String r8 = r1.toString()     // Catch:{ all -> 0x0068 }
                android.util.Log.e(r0, r8)     // Catch:{ all -> 0x0068 }
                monitor-exit(r7)
                return
            L_0x0068:
                r8 = move-exception
                monitor-exit(r7)
                throw r8
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.SoundClips.SoundPoolPlayer.play(int):void");
        }

        public void onLoadComplete(SoundPool soundPool, int i, int i2) {
            if (i2 != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("loading sound tracks failed (status=");
                sb.append(i2);
                sb.append(")");
                Log.e(TAG, sb.toString());
                int i3 = 0;
                while (true) {
                    int[] iArr = this.mSoundIDs;
                    if (i3 >= iArr.length) {
                        break;
                    } else if (iArr[i3] == i) {
                        iArr[i3] = 0;
                        break;
                    } else {
                        i3++;
                    }
                }
                return;
            }
            int i4 = 0;
            while (true) {
                int[] iArr2 = this.mSoundIDs;
                if (i4 >= iArr2.length) {
                    break;
                } else if (iArr2[i4] == i) {
                    this.mSoundIDReady[i4] = true;
                    break;
                } else {
                    i4++;
                }
            }
            if (i == this.mSoundIDToPlay) {
                this.mSoundIDToPlay = 0;
                this.mSoundPool.play(i, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }

    public static Player getPlayer(Context context) {
        if (ApiHelper.HAS_MEDIA_ACTION_SOUND) {
            return new MediaActionSoundPlayer();
        }
        return new SoundPoolPlayer(context);
    }

    public static int getAudioTypeForSoundPool() {
        return ApiHelper.getIntFieldIfExists(AudioManager.class, "STREAM_SYSTEM_ENFORCED", null, 2);
    }
}
