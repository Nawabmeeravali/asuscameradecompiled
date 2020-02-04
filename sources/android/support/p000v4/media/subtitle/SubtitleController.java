package android.support.p000v4.media.subtitle;

import android.content.Context;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.media.subtitle.SubtitleTrack.RenderingWidget;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

@RequiresApi(28)
@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.media.subtitle.SubtitleController */
public class SubtitleController {
    private static final int WHAT_HIDE = 2;
    private static final int WHAT_SELECT_DEFAULT_TRACK = 4;
    private static final int WHAT_SELECT_TRACK = 3;
    private static final int WHAT_SHOW = 1;
    private Anchor mAnchor;
    private final Callback mCallback;
    private CaptioningChangeListener mCaptioningChangeListener;
    private CaptioningManager mCaptioningManager;
    private Handler mHandler;
    private Listener mListener;
    private ArrayList<Renderer> mRenderers;
    private final Object mRenderersLock;
    private SubtitleTrack mSelectedTrack;
    private boolean mShowing;
    private MediaTimeProvider mTimeProvider;
    private boolean mTrackIsExplicit;
    private ArrayList<SubtitleTrack> mTracks;
    private final Object mTracksLock;
    private boolean mVisibilityIsExplicit;

    /* renamed from: android.support.v4.media.subtitle.SubtitleController$Anchor */
    public interface Anchor {
        Looper getSubtitleLooper();

        void setSubtitleWidget(RenderingWidget renderingWidget);
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleController$Listener */
    interface Listener {
        void onSubtitleTrackSelected(SubtitleTrack subtitleTrack);
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleController$MediaFormatUtil */
    static class MediaFormatUtil {
        MediaFormatUtil() {
        }

        static int getInteger(MediaFormat mediaFormat, String str, int i) {
            try {
                return mediaFormat.getInteger(str);
            } catch (ClassCastException | NullPointerException unused) {
                return i;
            }
        }
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleController$Renderer */
    public static abstract class Renderer {
        public abstract SubtitleTrack createTrack(MediaFormat mediaFormat);

        public abstract boolean supports(MediaFormat mediaFormat);
    }

    private void checkAnchorLooper() {
    }

    public SubtitleController(Context context) {
        this(context, null, null);
    }

    public SubtitleController(Context context, MediaTimeProvider mediaTimeProvider, Listener listener) {
        this.mRenderersLock = new Object();
        this.mTracksLock = new Object();
        this.mCallback = new Callback() {
            public boolean handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    SubtitleController.this.doShow();
                    return true;
                } else if (i == 2) {
                    SubtitleController.this.doHide();
                    return true;
                } else if (i == 3) {
                    SubtitleController.this.doSelectTrack((SubtitleTrack) message.obj);
                    return true;
                } else if (i != 4) {
                    return false;
                } else {
                    SubtitleController.this.doSelectDefaultTrack();
                    return true;
                }
            }
        };
        this.mCaptioningChangeListener = new CaptioningChangeListener() {
            public void onEnabledChanged(boolean z) {
                SubtitleController.this.selectDefaultTrack();
            }

            public void onLocaleChanged(Locale locale) {
                SubtitleController.this.selectDefaultTrack();
            }
        };
        this.mTrackIsExplicit = false;
        this.mVisibilityIsExplicit = false;
        this.mTimeProvider = mediaTimeProvider;
        this.mListener = listener;
        this.mRenderers = new ArrayList<>();
        this.mShowing = false;
        this.mTracks = new ArrayList<>();
        this.mCaptioningManager = (CaptioningManager) context.getSystemService("captioning");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
        super.finalize();
    }

    public SubtitleTrack[] getTracks() {
        SubtitleTrack[] subtitleTrackArr;
        synchronized (this.mTracksLock) {
            subtitleTrackArr = new SubtitleTrack[this.mTracks.size()];
            this.mTracks.toArray(subtitleTrackArr);
        }
        return subtitleTrackArr;
    }

    public SubtitleTrack getSelectedTrack() {
        return this.mSelectedTrack;
    }

    private RenderingWidget getRenderingWidget() {
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack == null) {
            return null;
        }
        return subtitleTrack.getRenderingWidget();
    }

    public boolean selectTrack(SubtitleTrack subtitleTrack) {
        if (subtitleTrack != null && !this.mTracks.contains(subtitleTrack)) {
            return false;
        }
        processOnAnchor(this.mHandler.obtainMessage(3, subtitleTrack));
        return true;
    }

    /* access modifiers changed from: private */
    public void doSelectTrack(SubtitleTrack subtitleTrack) {
        this.mTrackIsExplicit = true;
        SubtitleTrack subtitleTrack2 = this.mSelectedTrack;
        if (subtitleTrack2 != subtitleTrack) {
            if (subtitleTrack2 != null) {
                subtitleTrack2.hide();
                this.mSelectedTrack.setTimeProvider(null);
            }
            this.mSelectedTrack = subtitleTrack;
            Anchor anchor = this.mAnchor;
            if (anchor != null) {
                anchor.setSubtitleWidget(getRenderingWidget());
            }
            SubtitleTrack subtitleTrack3 = this.mSelectedTrack;
            if (subtitleTrack3 != null) {
                subtitleTrack3.setTimeProvider(this.mTimeProvider);
                this.mSelectedTrack.show();
            }
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onSubtitleTrackSelected(subtitleTrack);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.support.p000v4.media.subtitle.SubtitleTrack getDefaultTrack() {
        /*
            r15 = this;
            android.view.accessibility.CaptioningManager r0 = r15.mCaptioningManager
            java.util.Locale r0 = r0.getLocale()
            if (r0 != 0) goto L_0x000d
            java.util.Locale r1 = java.util.Locale.getDefault()
            goto L_0x000e
        L_0x000d:
            r1 = r0
        L_0x000e:
            android.view.accessibility.CaptioningManager r2 = r15.mCaptioningManager
            boolean r2 = r2.isEnabled()
            r3 = 1
            r2 = r2 ^ r3
            java.lang.Object r4 = r15.mTracksLock
            monitor-enter(r4)
            java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack> r15 = r15.mTracks     // Catch:{ all -> 0x00ac }
            java.util.Iterator r15 = r15.iterator()     // Catch:{ all -> 0x00ac }
            r5 = 0
            r6 = -1
        L_0x0021:
            boolean r7 = r15.hasNext()     // Catch:{ all -> 0x00ac }
            if (r7 == 0) goto L_0x00aa
            java.lang.Object r7 = r15.next()     // Catch:{ all -> 0x00ac }
            android.support.v4.media.subtitle.SubtitleTrack r7 = (android.support.p000v4.media.subtitle.SubtitleTrack) r7     // Catch:{ all -> 0x00ac }
            android.media.MediaFormat r8 = r7.getFormat()     // Catch:{ all -> 0x00ac }
            java.lang.String r9 = "language"
            java.lang.String r9 = r8.getString(r9)     // Catch:{ all -> 0x00ac }
            java.lang.String r10 = "is-forced-subtitle"
            r11 = 0
            int r10 = android.support.p000v4.media.subtitle.SubtitleController.MediaFormatUtil.getInteger(r8, r10, r11)     // Catch:{ all -> 0x00ac }
            if (r10 == 0) goto L_0x0042
            r10 = r3
            goto L_0x0043
        L_0x0042:
            r10 = r11
        L_0x0043:
            java.lang.String r12 = "is-autoselect"
            int r12 = android.support.p000v4.media.subtitle.SubtitleController.MediaFormatUtil.getInteger(r8, r12, r3)     // Catch:{ all -> 0x00ac }
            if (r12 == 0) goto L_0x004d
            r12 = r3
            goto L_0x004e
        L_0x004d:
            r12 = r11
        L_0x004e:
            java.lang.String r13 = "is-default"
            int r8 = android.support.p000v4.media.subtitle.SubtitleController.MediaFormatUtil.getInteger(r8, r13, r11)     // Catch:{ all -> 0x00ac }
            if (r8 == 0) goto L_0x0058
            r8 = r3
            goto L_0x0059
        L_0x0058:
            r8 = r11
        L_0x0059:
            if (r1 == 0) goto L_0x007e
            java.lang.String r13 = r1.getLanguage()     // Catch:{ all -> 0x00ac }
            java.lang.String r14 = ""
            boolean r13 = r13.equals(r14)     // Catch:{ all -> 0x00ac }
            if (r13 != 0) goto L_0x007e
            java.lang.String r13 = r1.getISO3Language()     // Catch:{ all -> 0x00ac }
            boolean r13 = r13.equals(r9)     // Catch:{ all -> 0x00ac }
            if (r13 != 0) goto L_0x007e
            java.lang.String r13 = r1.getLanguage()     // Catch:{ all -> 0x00ac }
            boolean r9 = r13.equals(r9)     // Catch:{ all -> 0x00ac }
            if (r9 == 0) goto L_0x007c
            goto L_0x007e
        L_0x007c:
            r9 = r11
            goto L_0x007f
        L_0x007e:
            r9 = r3
        L_0x007f:
            if (r10 == 0) goto L_0x0083
            r13 = r11
            goto L_0x0085
        L_0x0083:
            r13 = 8
        L_0x0085:
            if (r0 != 0) goto L_0x008b
            if (r8 == 0) goto L_0x008b
            r14 = 4
            goto L_0x008c
        L_0x008b:
            r14 = r11
        L_0x008c:
            int r13 = r13 + r14
            if (r12 == 0) goto L_0x0090
            goto L_0x0091
        L_0x0090:
            r11 = 2
        L_0x0091:
            int r13 = r13 + r11
            int r13 = r13 + r9
            if (r2 == 0) goto L_0x0098
            if (r10 != 0) goto L_0x0098
            goto L_0x0021
        L_0x0098:
            if (r0 != 0) goto L_0x009c
            if (r8 != 0) goto L_0x00a4
        L_0x009c:
            if (r9 == 0) goto L_0x0021
            if (r12 != 0) goto L_0x00a4
            if (r10 != 0) goto L_0x00a4
            if (r0 == 0) goto L_0x0021
        L_0x00a4:
            if (r13 <= r6) goto L_0x0021
            r5 = r7
            r6 = r13
            goto L_0x0021
        L_0x00aa:
            monitor-exit(r4)     // Catch:{ all -> 0x00ac }
            return r5
        L_0x00ac:
            r15 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x00ac }
            throw r15
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.SubtitleController.getDefaultTrack():android.support.v4.media.subtitle.SubtitleTrack");
    }

    public void selectDefaultTrack() {
        processOnAnchor(this.mHandler.obtainMessage(4));
    }

    /* access modifiers changed from: private */
    public void doSelectDefaultTrack() {
        if (this.mTrackIsExplicit) {
            if (!this.mVisibilityIsExplicit) {
                if (!this.mCaptioningManager.isEnabled()) {
                    SubtitleTrack subtitleTrack = this.mSelectedTrack;
                    if (subtitleTrack == null || MediaFormatUtil.getInteger(subtitleTrack.getFormat(), "is-forced-subtitle", 0) == 0) {
                        SubtitleTrack subtitleTrack2 = this.mSelectedTrack;
                        if (subtitleTrack2 != null && subtitleTrack2.getTrackType() == 4) {
                            hide();
                        }
                        this.mVisibilityIsExplicit = false;
                    }
                }
                show();
                this.mVisibilityIsExplicit = false;
            } else {
                return;
            }
        }
        SubtitleTrack defaultTrack = getDefaultTrack();
        if (defaultTrack != null) {
            selectTrack(defaultTrack);
            this.mTrackIsExplicit = false;
            if (!this.mVisibilityIsExplicit) {
                show();
                this.mVisibilityIsExplicit = false;
            }
        }
    }

    public void reset() {
        checkAnchorLooper();
        hide();
        selectTrack(null);
        this.mTracks.clear();
        this.mTrackIsExplicit = false;
        this.mVisibilityIsExplicit = false;
        this.mCaptioningManager.removeCaptioningChangeListener(this.mCaptioningChangeListener);
    }

    public SubtitleTrack addTrack(MediaFormat mediaFormat) {
        synchronized (this.mRenderersLock) {
            Iterator it = this.mRenderers.iterator();
            while (it.hasNext()) {
                Renderer renderer = (Renderer) it.next();
                if (renderer.supports(mediaFormat)) {
                    SubtitleTrack createTrack = renderer.createTrack(mediaFormat);
                    if (createTrack != null) {
                        synchronized (this.mTracksLock) {
                            if (this.mTracks.size() == 0) {
                                this.mCaptioningManager.addCaptioningChangeListener(this.mCaptioningChangeListener);
                            }
                            this.mTracks.add(createTrack);
                        }
                        return createTrack;
                    }
                }
            }
            return null;
        }
    }

    public void show() {
        processOnAnchor(this.mHandler.obtainMessage(1));
    }

    /* access modifiers changed from: private */
    public void doShow() {
        this.mShowing = true;
        this.mVisibilityIsExplicit = true;
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack != null) {
            subtitleTrack.show();
        }
    }

    public void hide() {
        processOnAnchor(this.mHandler.obtainMessage(2));
    }

    /* access modifiers changed from: private */
    public void doHide() {
        this.mVisibilityIsExplicit = true;
        SubtitleTrack subtitleTrack = this.mSelectedTrack;
        if (subtitleTrack != null) {
            subtitleTrack.hide();
        }
        this.mShowing = false;
    }

    public void registerRenderer(Renderer renderer) {
        synchronized (this.mRenderersLock) {
            if (!this.mRenderers.contains(renderer)) {
                this.mRenderers.add(renderer);
            }
        }
    }

    public boolean hasRendererFor(MediaFormat mediaFormat) {
        synchronized (this.mRenderersLock) {
            Iterator it = this.mRenderers.iterator();
            while (it.hasNext()) {
                if (((Renderer) it.next()).supports(mediaFormat)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setAnchor(Anchor anchor) {
        Anchor anchor2 = this.mAnchor;
        if (anchor2 != anchor) {
            if (anchor2 != null) {
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(null);
            }
            this.mAnchor = anchor;
            this.mHandler = null;
            Anchor anchor3 = this.mAnchor;
            if (anchor3 != null) {
                this.mHandler = new Handler(anchor3.getSubtitleLooper(), this.mCallback);
                checkAnchorLooper();
                this.mAnchor.setSubtitleWidget(getRenderingWidget());
            }
        }
    }

    private void processOnAnchor(Message message) {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mHandler.dispatchMessage(message);
        } else {
            this.mHandler.sendMessage(message);
        }
    }
}
