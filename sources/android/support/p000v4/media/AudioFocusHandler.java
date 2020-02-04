package android.support.p000v4.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.support.annotation.GuardedBy;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

@VisibleForTesting(otherwise = 3)
@RestrictTo({Scope.LIBRARY})
/* renamed from: android.support.v4.media.AudioFocusHandler */
public class AudioFocusHandler {
    private static final boolean DEBUG = false;
    private static final String TAG = "AudioFocusHandler";
    private final AudioFocusHandlerImpl mImpl;

    /* renamed from: android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImpl */
    interface AudioFocusHandlerImpl {
        void close();

        boolean onPauseRequested();

        boolean onPlayRequested();

        void onPlayerStateChanged(int i);

        void sendIntent(Intent intent);
    }

    /* renamed from: android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase */
    private static class AudioFocusHandlerImplBase implements AudioFocusHandlerImpl {
        private static final float VOLUME_DUCK_FACTOR = 0.2f;
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public AudioAttributesCompat mAudioAttributes;
        private final OnAudioFocusChangeListener mAudioFocusListener = new AudioFocusListener();
        private final AudioManager mAudioManager;
        private final BroadcastReceiver mBecomingNoisyIntentReceiver = new NoisyIntentReceiver();
        @GuardedBy("mLock")
        private boolean mHasAudioFocus;
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public boolean mHasRegisteredReceiver;
        private final IntentFilter mIntentFilter = new IntentFilter("android.media.AUDIO_BECOMING_NOISY");
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public boolean mResumeWhenAudioFocusGain;
        /* access modifiers changed from: private */
        public final MediaSession2 mSession;

        /* renamed from: android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase$AudioFocusListener */
        private class AudioFocusListener implements OnAudioFocusChangeListener {
            private float mPlayerDuckingVolume;
            private float mPlayerVolumeBeforeDucking;

            private AudioFocusListener() {
            }

            /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
                return;
             */
            /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onAudioFocusChange(int r5) {
                /*
                    r4 = this;
                    r0 = -3
                    r1 = 1
                    if (r5 == r0) goto L_0x009b
                    r0 = -2
                    if (r5 == r0) goto L_0x0081
                    r0 = -1
                    if (r5 == r0) goto L_0x0066
                    if (r5 == r1) goto L_0x000e
                    goto L_0x00ea
                L_0x000e:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r5 = r5.mSession
                    int r5 = r5.getPlayerState()
                    if (r5 != r1) goto L_0x003b
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r5 = r5.mLock
                    monitor-enter(r5)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r0 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x0038 }
                    boolean r0 = r0.mResumeWhenAudioFocusGain     // Catch:{ all -> 0x0038 }
                    if (r0 != 0) goto L_0x002c
                    monitor-exit(r5)     // Catch:{ all -> 0x0038 }
                    goto L_0x00ea
                L_0x002c:
                    monitor-exit(r5)     // Catch:{ all -> 0x0038 }
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r4 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r4 = r4.mSession
                    r4.play()
                    goto L_0x00ea
                L_0x0038:
                    r4 = move-exception
                    monitor-exit(r5)     // Catch:{ all -> 0x0038 }
                    throw r4
                L_0x003b:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r5 = r5.mSession
                    android.support.v4.media.BaseMediaPlayer r5 = r5.getPlayer()
                    if (r5 == 0) goto L_0x00ea
                    float r0 = r5.getPlayerVolume()
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r1 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r1 = r1.mLock
                    monitor-enter(r1)
                    float r2 = r4.mPlayerDuckingVolume     // Catch:{ all -> 0x0063 }
                    int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
                    if (r0 == 0) goto L_0x005b
                    monitor-exit(r1)     // Catch:{ all -> 0x0063 }
                    goto L_0x00ea
                L_0x005b:
                    float r4 = r4.mPlayerVolumeBeforeDucking     // Catch:{ all -> 0x0063 }
                    monitor-exit(r1)     // Catch:{ all -> 0x0063 }
                    r5.setPlayerVolume(r4)
                    goto L_0x00ea
                L_0x0063:
                    r4 = move-exception
                    monitor-exit(r1)     // Catch:{ all -> 0x0063 }
                    throw r4
                L_0x0066:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r5 = r5.mSession
                    r5.pause()
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r5 = r5.mLock
                    monitor-enter(r5)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r4 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x007e }
                    r0 = 0
                    r4.mResumeWhenAudioFocusGain = r0     // Catch:{ all -> 0x007e }
                    monitor-exit(r5)     // Catch:{ all -> 0x007e }
                    goto L_0x00ea
                L_0x007e:
                    r4 = move-exception
                    monitor-exit(r5)     // Catch:{ all -> 0x007e }
                    throw r4
                L_0x0081:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r5 = r5.mSession
                    r5.pause()
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r5 = r5.mLock
                    monitor-enter(r5)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r4 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x0098 }
                    r4.mResumeWhenAudioFocusGain = r1     // Catch:{ all -> 0x0098 }
                    monitor-exit(r5)     // Catch:{ all -> 0x0098 }
                    goto L_0x00ea
                L_0x0098:
                    r4 = move-exception
                    monitor-exit(r5)     // Catch:{ all -> 0x0098 }
                    throw r4
                L_0x009b:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r5 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r5 = r5.mLock
                    monitor-enter(r5)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r0 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x00eb }
                    android.support.v4.media.AudioAttributesCompat r0 = r0.mAudioAttributes     // Catch:{ all -> 0x00eb }
                    if (r0 != 0) goto L_0x00ac
                    monitor-exit(r5)     // Catch:{ all -> 0x00eb }
                    goto L_0x00ea
                L_0x00ac:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r0 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x00eb }
                    android.support.v4.media.AudioAttributesCompat r0 = r0.mAudioAttributes     // Catch:{ all -> 0x00eb }
                    int r0 = r0.getContentType()     // Catch:{ all -> 0x00eb }
                    if (r0 != r1) goto L_0x00c2
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r4 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x00eb }
                    android.support.v4.media.MediaSession2 r4 = r4.mSession     // Catch:{ all -> 0x00eb }
                    r4.pause()     // Catch:{ all -> 0x00eb }
                    goto L_0x00e9
                L_0x00c2:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r0 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x00eb }
                    android.support.v4.media.MediaSession2 r0 = r0.mSession     // Catch:{ all -> 0x00eb }
                    android.support.v4.media.BaseMediaPlayer r0 = r0.getPlayer()     // Catch:{ all -> 0x00eb }
                    if (r0 == 0) goto L_0x00e9
                    float r1 = r0.getPlayerVolume()     // Catch:{ all -> 0x00eb }
                    r2 = 1045220557(0x3e4ccccd, float:0.2)
                    float r2 = r2 * r1
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r3 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x00eb }
                    java.lang.Object r3 = r3.mLock     // Catch:{ all -> 0x00eb }
                    monitor-enter(r3)     // Catch:{ all -> 0x00eb }
                    r4.mPlayerVolumeBeforeDucking = r1     // Catch:{ all -> 0x00e6 }
                    r4.mPlayerDuckingVolume = r2     // Catch:{ all -> 0x00e6 }
                    monitor-exit(r3)     // Catch:{ all -> 0x00e6 }
                    r0.setPlayerVolume(r2)     // Catch:{ all -> 0x00eb }
                    goto L_0x00e9
                L_0x00e6:
                    r4 = move-exception
                    monitor-exit(r3)     // Catch:{ all -> 0x00e6 }
                    throw r4     // Catch:{ all -> 0x00eb }
                L_0x00e9:
                    monitor-exit(r5)     // Catch:{ all -> 0x00eb }
                L_0x00ea:
                    return
                L_0x00eb:
                    r4 = move-exception
                    monitor-exit(r5)     // Catch:{ all -> 0x00eb }
                    throw r4
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.AudioFocusListener.onAudioFocusChange(int):void");
            }
        }

        /* renamed from: android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase$NoisyIntentReceiver */
        private class NoisyIntentReceiver extends BroadcastReceiver {
            private NoisyIntentReceiver() {
            }

            /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
                r2 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$200(r1.this$0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
                monitor-enter(r2);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
                if (android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$400(r1.this$0) != null) goto L_0x002f;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
                monitor-exit(r2);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
                r3 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$400(r1.this$0).getUsage();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
                monitor-exit(r2);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
                if (r3 == 1) goto L_0x005a;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:22:0x003f, code lost:
                if (r3 == 14) goto L_0x0042;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
                r1 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$500(r1.this$0).getPlayer();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x004c, code lost:
                if (r1 == null) goto L_0x0067;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:0x004e, code lost:
                r1.setPlayerVolume(r1.getPlayerVolume() * android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.VOLUME_DUCK_FACTOR);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:26:0x005a, code lost:
                android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.access$500(r1.this$0).pause();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:31:0x0067, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
                if ("android.media.AUDIO_BECOMING_NOISY".equals(r3.getAction()) == false) goto L_0x0067;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r2, android.content.Intent r3) {
                /*
                    r1 = this;
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r2 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r2 = r2.mLock
                    monitor-enter(r2)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r0 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x0068 }
                    boolean r0 = r0.mHasRegisteredReceiver     // Catch:{ all -> 0x0068 }
                    if (r0 != 0) goto L_0x0011
                    monitor-exit(r2)     // Catch:{ all -> 0x0068 }
                    return
                L_0x0011:
                    monitor-exit(r2)     // Catch:{ all -> 0x0068 }
                    java.lang.String r2 = r3.getAction()
                    java.lang.String r3 = "android.media.AUDIO_BECOMING_NOISY"
                    boolean r2 = r3.equals(r2)
                    if (r2 == 0) goto L_0x0067
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r2 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    java.lang.Object r2 = r2.mLock
                    monitor-enter(r2)
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r3 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x0064 }
                    android.support.v4.media.AudioAttributesCompat r3 = r3.mAudioAttributes     // Catch:{ all -> 0x0064 }
                    if (r3 != 0) goto L_0x002f
                    monitor-exit(r2)     // Catch:{ all -> 0x0064 }
                    return
                L_0x002f:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r3 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this     // Catch:{ all -> 0x0064 }
                    android.support.v4.media.AudioAttributesCompat r3 = r3.mAudioAttributes     // Catch:{ all -> 0x0064 }
                    int r3 = r3.getUsage()     // Catch:{ all -> 0x0064 }
                    monitor-exit(r2)     // Catch:{ all -> 0x0064 }
                    r2 = 1
                    if (r3 == r2) goto L_0x005a
                    r2 = 14
                    if (r3 == r2) goto L_0x0042
                    goto L_0x0067
                L_0x0042:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r1 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r1 = r1.mSession
                    android.support.v4.media.BaseMediaPlayer r1 = r1.getPlayer()
                    if (r1 == 0) goto L_0x0067
                    float r2 = r1.getPlayerVolume()
                    r3 = 1045220557(0x3e4ccccd, float:0.2)
                    float r2 = r2 * r3
                    r1.setPlayerVolume(r2)
                    goto L_0x0067
                L_0x005a:
                    android.support.v4.media.AudioFocusHandler$AudioFocusHandlerImplBase r1 = android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.this
                    android.support.v4.media.MediaSession2 r1 = r1.mSession
                    r1.pause()
                    goto L_0x0067
                L_0x0064:
                    r1 = move-exception
                    monitor-exit(r2)     // Catch:{ all -> 0x0064 }
                    throw r1
                L_0x0067:
                    return
                L_0x0068:
                    r1 = move-exception
                    monitor-exit(r2)     // Catch:{ all -> 0x0068 }
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.NoisyIntentReceiver.onReceive(android.content.Context, android.content.Intent):void");
            }
        }

        AudioFocusHandlerImplBase(Context context, MediaSession2 mediaSession2) {
            this.mSession = mediaSession2;
            this.mAudioManager = (AudioManager) context.getSystemService("audio");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003d, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateAudioAttributesIfNeeded() {
            /*
                r3 = this;
                android.support.v4.media.MediaSession2 r0 = r3.mSession
                android.support.v4.media.VolumeProviderCompat r0 = r0.getVolumeProvider()
                r1 = 0
                if (r0 == 0) goto L_0x000a
                goto L_0x0018
            L_0x000a:
                android.support.v4.media.MediaSession2 r0 = r3.mSession
                android.support.v4.media.BaseMediaPlayer r0 = r0.getPlayer()
                if (r0 != 0) goto L_0x0013
                goto L_0x0018
            L_0x0013:
                android.support.v4.media.AudioAttributesCompat r0 = r0.getAudioAttributes()
                r1 = r0
            L_0x0018:
                java.lang.Object r0 = r3.mLock
                monitor-enter(r0)
                android.support.v4.media.AudioAttributesCompat r2 = r3.mAudioAttributes     // Catch:{ all -> 0x003e }
                boolean r2 = android.support.p000v4.util.ObjectsCompat.equals(r1, r2)     // Catch:{ all -> 0x003e }
                if (r2 == 0) goto L_0x0025
                monitor-exit(r0)     // Catch:{ all -> 0x003e }
                return
            L_0x0025:
                r3.mAudioAttributes = r1     // Catch:{ all -> 0x003e }
                boolean r1 = r3.mHasAudioFocus     // Catch:{ all -> 0x003e }
                if (r1 == 0) goto L_0x003c
                boolean r1 = r3.requestAudioFocusLocked()     // Catch:{ all -> 0x003e }
                r3.mHasAudioFocus = r1     // Catch:{ all -> 0x003e }
                boolean r3 = r3.mHasAudioFocus     // Catch:{ all -> 0x003e }
                if (r3 != 0) goto L_0x003c
                java.lang.String r3 = "AudioFocusHandler"
                java.lang.String r1 = "Failed to regain audio focus."
                android.util.Log.w(r3, r1)     // Catch:{ all -> 0x003e }
            L_0x003c:
                monitor-exit(r0)     // Catch:{ all -> 0x003e }
                return
            L_0x003e:
                r3 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x003e }
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.AudioFocusHandler.AudioFocusHandlerImplBase.updateAudioAttributesIfNeeded():void");
        }

        public boolean onPlayRequested() {
            updateAudioAttributesIfNeeded();
            synchronized (this.mLock) {
                if (!requestAudioFocusLocked()) {
                    return false;
                }
                return true;
            }
        }

        public boolean onPauseRequested() {
            synchronized (this.mLock) {
                this.mResumeWhenAudioFocusGain = false;
            }
            return true;
        }

        public void onPlayerStateChanged(int i) {
            synchronized (this.mLock) {
                if (i == 0) {
                    abandonAudioFocusLocked();
                } else if (i == 1) {
                    updateAudioAttributesIfNeeded();
                    unregisterReceiverLocked();
                } else if (i == 2) {
                    updateAudioAttributesIfNeeded();
                    registerReceiverLocked();
                } else if (i == 3) {
                    abandonAudioFocusLocked();
                    unregisterReceiverLocked();
                }
            }
        }

        public void close() {
            synchronized (this.mLock) {
                unregisterReceiverLocked();
                abandonAudioFocusLocked();
            }
        }

        public void sendIntent(Intent intent) {
            this.mBecomingNoisyIntentReceiver.onReceive(this.mSession.getContext(), intent);
        }

        @GuardedBy("mLock")
        private boolean requestAudioFocusLocked() {
            int convertAudioAttributesToFocusGainLocked = convertAudioAttributesToFocusGainLocked();
            if (convertAudioAttributesToFocusGainLocked == 0) {
                return true;
            }
            int requestAudioFocus = this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, this.mAudioAttributes.getVolumeControlStream(), convertAudioAttributesToFocusGainLocked);
            if (requestAudioFocus == 1) {
                this.mHasAudioFocus = true;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("requestAudioFocus(");
                sb.append(convertAudioAttributesToFocusGainLocked);
                sb.append(") failed (return=");
                sb.append(requestAudioFocus);
                sb.append(") playback wouldn't start.");
                Log.w(AudioFocusHandler.TAG, sb.toString());
                this.mHasAudioFocus = false;
            }
            this.mResumeWhenAudioFocusGain = false;
            return this.mHasAudioFocus;
        }

        @GuardedBy("mLock")
        private void abandonAudioFocusLocked() {
            if (this.mHasAudioFocus) {
                this.mAudioManager.abandonAudioFocus(this.mAudioFocusListener);
                this.mHasAudioFocus = false;
                this.mResumeWhenAudioFocusGain = false;
            }
        }

        @GuardedBy("mLock")
        private void registerReceiverLocked() {
            if (!this.mHasRegisteredReceiver) {
                this.mSession.getContext().registerReceiver(this.mBecomingNoisyIntentReceiver, this.mIntentFilter);
                this.mHasRegisteredReceiver = true;
            }
        }

        @GuardedBy("mLock")
        private void unregisterReceiverLocked() {
            if (this.mHasRegisteredReceiver) {
                this.mSession.getContext().unregisterReceiver(this.mBecomingNoisyIntentReceiver);
                this.mHasRegisteredReceiver = false;
            }
        }

        @GuardedBy("mLock")
        private int convertAudioAttributesToFocusGainLocked() {
            AudioAttributesCompat audioAttributesCompat = this.mAudioAttributes;
            if (audioAttributesCompat == null) {
                return 0;
            }
            switch (audioAttributesCompat.getUsage()) {
                case 0:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 16:
                    return 3;
                case 1:
                case 14:
                    return 1;
                case 2:
                case 3:
                case 4:
                    return 2;
                default:
                    return 0;
            }
        }
    }

    AudioFocusHandler(Context context, MediaSession2 mediaSession2) {
        this.mImpl = new AudioFocusHandlerImplBase(context, mediaSession2);
    }

    public boolean onPlayRequested() {
        return this.mImpl.onPlayRequested();
    }

    public boolean onPauseRequested() {
        return this.mImpl.onPauseRequested();
    }

    public void onPlayerStateChanged(int i) {
        this.mImpl.onPlayerStateChanged(i);
    }

    public void close() {
        this.mImpl.close();
    }

    public void sendIntent(Intent intent) {
        this.mImpl.sendIntent(intent);
    }
}
