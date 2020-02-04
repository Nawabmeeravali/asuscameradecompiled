package android.support.p000v4.media.subtitle;

import android.graphics.Canvas;
import android.media.MediaFormat;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.media.SubtitleData2;
import android.support.p000v4.media.subtitle.MediaTimeProvider.OnMediaTimeListener;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import com.asus.scenedetectlib.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

@RequiresApi(28)
@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.media.subtitle.SubtitleTrack */
public abstract class SubtitleTrack implements OnMediaTimeListener {
    private static final String TAG = "SubtitleTrack";
    public boolean DEBUG = false;
    /* access modifiers changed from: private */
    public final ArrayList<Cue> mActiveCues = new ArrayList<>();
    private CueList mCues;
    private MediaFormat mFormat;
    protected Handler mHandler = new Handler();
    private long mLastTimeMs;
    private long mLastUpdateTimeMs;
    private long mNextScheduledTimeMs = -1;
    /* access modifiers changed from: private */
    public Runnable mRunnable;
    private final LongSparseArray<Run> mRunsByEndTime = new LongSparseArray<>();
    private final LongSparseArray<Run> mRunsByID = new LongSparseArray<>();
    protected MediaTimeProvider mTimeProvider;
    protected boolean mVisible;

    /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$1 */
    class C04201 implements Runnable {
        final /* synthetic */ SubtitleTrack this$0;
        final /* synthetic */ long val$thenMs;
        final /* synthetic */ SubtitleTrack val$track;

        public void run() {
            synchronized (this.val$track) {
                this.this$0.mRunnable = null;
                this.this$0.updateActiveCues(true, this.val$thenMs);
                this.this$0.updateView(this.this$0.mActiveCues);
            }
        }
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$Cue */
    static class Cue {
        public long mEndTimeMs;
        public long[] mInnerTimesMs;
        public Cue mNextInRun;
        public long mRunID;
        public long mStartTimeMs;

        public void onTime(long j) {
        }

        Cue() {
        }
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$CueList */
    static class CueList {
        private static final String TAG = "CueList";
        public boolean DEBUG = false;
        /* access modifiers changed from: private */
        public SortedMap<Long, ArrayList<Cue>> mCues = new TreeMap();

        /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$CueList$EntryIterator */
        class EntryIterator implements Iterator<Pair<Long, Cue>> {
            private long mCurrentTimeMs;
            private boolean mDone;
            private Pair<Long, Cue> mLastEntry;
            private Iterator<Cue> mLastListIterator;
            private Iterator<Cue> mListIterator;
            private SortedMap<Long, ArrayList<Cue>> mRemainingCues;

            public boolean hasNext() {
                return !this.mDone;
            }

            public Pair<Long, Cue> next() {
                if (!this.mDone) {
                    this.mLastEntry = new Pair<>(Long.valueOf(this.mCurrentTimeMs), this.mListIterator.next());
                    Iterator<Cue> it = this.mListIterator;
                    this.mLastListIterator = it;
                    if (!it.hasNext()) {
                        nextKey();
                    }
                    return this.mLastEntry;
                }
                throw new NoSuchElementException(BuildConfig.FLAVOR);
            }

            public void remove() {
                if (this.mLastListIterator != null) {
                    Pair<Long, Cue> pair = this.mLastEntry;
                    if (((Cue) pair.second).mEndTimeMs == ((Long) pair.first).longValue()) {
                        this.mLastListIterator.remove();
                        this.mLastListIterator = null;
                        if (((ArrayList) CueList.this.mCues.get(this.mLastEntry.first)).size() == 0) {
                            CueList.this.mCues.remove(this.mLastEntry.first);
                        }
                        Cue cue = (Cue) this.mLastEntry.second;
                        CueList.this.removeEvent(cue, cue.mStartTimeMs);
                        long[] jArr = cue.mInnerTimesMs;
                        if (jArr != null) {
                            for (long access$400 : jArr) {
                                CueList.this.removeEvent(cue, access$400);
                            }
                            return;
                        }
                        return;
                    }
                }
                throw new IllegalStateException(BuildConfig.FLAVOR);
            }

            EntryIterator(SortedMap<Long, ArrayList<Cue>> sortedMap) {
                if (CueList.this.DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sortedMap);
                    sb.append(BuildConfig.FLAVOR);
                    Log.v(CueList.TAG, sb.toString());
                }
                this.mRemainingCues = sortedMap;
                this.mLastListIterator = null;
                nextKey();
            }

            /* JADX WARNING: Can't wrap try/catch for region: R(7:4|5|6|7|8|9|10) */
            /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0039 */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void nextKey() {
                /*
                    r6 = this;
                L_0x0000:
                    r0 = 0
                    java.util.SortedMap<java.lang.Long, java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue>> r1 = r6.mRemainingCues     // Catch:{ NoSuchElementException -> 0x004f }
                    if (r1 == 0) goto L_0x0047
                    java.util.SortedMap<java.lang.Long, java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue>> r1 = r6.mRemainingCues     // Catch:{ NoSuchElementException -> 0x004f }
                    java.lang.Object r1 = r1.firstKey()     // Catch:{ NoSuchElementException -> 0x004f }
                    java.lang.Long r1 = (java.lang.Long) r1     // Catch:{ NoSuchElementException -> 0x004f }
                    long r1 = r1.longValue()     // Catch:{ NoSuchElementException -> 0x004f }
                    r6.mCurrentTimeMs = r1     // Catch:{ NoSuchElementException -> 0x004f }
                    java.util.SortedMap<java.lang.Long, java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue>> r1 = r6.mRemainingCues     // Catch:{ NoSuchElementException -> 0x004f }
                    long r2 = r6.mCurrentTimeMs     // Catch:{ NoSuchElementException -> 0x004f }
                    java.lang.Long r2 = java.lang.Long.valueOf(r2)     // Catch:{ NoSuchElementException -> 0x004f }
                    java.lang.Object r1 = r1.get(r2)     // Catch:{ NoSuchElementException -> 0x004f }
                    java.util.ArrayList r1 = (java.util.ArrayList) r1     // Catch:{ NoSuchElementException -> 0x004f }
                    java.util.Iterator r1 = r1.iterator()     // Catch:{ NoSuchElementException -> 0x004f }
                    r6.mListIterator = r1     // Catch:{ NoSuchElementException -> 0x004f }
                    java.util.SortedMap<java.lang.Long, java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue>> r1 = r6.mRemainingCues     // Catch:{ IllegalArgumentException -> 0x0039 }
                    long r2 = r6.mCurrentTimeMs     // Catch:{ IllegalArgumentException -> 0x0039 }
                    r4 = 1
                    long r2 = r2 + r4
                    java.lang.Long r2 = java.lang.Long.valueOf(r2)     // Catch:{ IllegalArgumentException -> 0x0039 }
                    java.util.SortedMap r1 = r1.tailMap(r2)     // Catch:{ IllegalArgumentException -> 0x0039 }
                    r6.mRemainingCues = r1     // Catch:{ IllegalArgumentException -> 0x0039 }
                    goto L_0x003b
                L_0x0039:
                    r6.mRemainingCues = r0     // Catch:{ NoSuchElementException -> 0x004f }
                L_0x003b:
                    r1 = 0
                    r6.mDone = r1     // Catch:{ NoSuchElementException -> 0x004f }
                    java.util.Iterator<android.support.v4.media.subtitle.SubtitleTrack$Cue> r0 = r6.mListIterator
                    boolean r0 = r0.hasNext()
                    if (r0 == 0) goto L_0x0000
                    return
                L_0x0047:
                    java.util.NoSuchElementException r1 = new java.util.NoSuchElementException     // Catch:{ NoSuchElementException -> 0x004f }
                    java.lang.String r2 = ""
                    r1.<init>(r2)     // Catch:{ NoSuchElementException -> 0x004f }
                    throw r1     // Catch:{ NoSuchElementException -> 0x004f }
                L_0x004f:
                    r1 = 1
                    r6.mDone = r1
                    r6.mRemainingCues = r0
                    r6.mListIterator = r0
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.SubtitleTrack.CueList.EntryIterator.nextKey():void");
            }
        }

        private boolean addEvent(Cue cue, long j) {
            ArrayList arrayList = (ArrayList) this.mCues.get(Long.valueOf(j));
            if (arrayList == null) {
                arrayList = new ArrayList(2);
                this.mCues.put(Long.valueOf(j), arrayList);
            } else if (arrayList.contains(cue)) {
                return false;
            }
            arrayList.add(cue);
            return true;
        }

        /* access modifiers changed from: private */
        public void removeEvent(Cue cue, long j) {
            ArrayList arrayList = (ArrayList) this.mCues.get(Long.valueOf(j));
            if (arrayList != null) {
                arrayList.remove(cue);
                if (arrayList.size() == 0) {
                    this.mCues.remove(Long.valueOf(j));
                }
            }
        }

        public void add(Cue cue) {
            long j = cue.mStartTimeMs;
            if (j < cue.mEndTimeMs && addEvent(cue, j)) {
                long j2 = cue.mStartTimeMs;
                long[] jArr = cue.mInnerTimesMs;
                if (jArr != null) {
                    for (long j3 : jArr) {
                        if (j3 > j2 && j3 < cue.mEndTimeMs) {
                            addEvent(cue, j3);
                            j2 = j3;
                        }
                    }
                }
                addEvent(cue, cue.mEndTimeMs);
            }
        }

        public void remove(Cue cue) {
            removeEvent(cue, cue.mStartTimeMs);
            long[] jArr = cue.mInnerTimesMs;
            if (jArr != null) {
                for (long removeEvent : jArr) {
                    removeEvent(cue, removeEvent);
                }
            }
            removeEvent(cue, cue.mEndTimeMs);
        }

        public Iterable<Pair<Long, Cue>> entriesBetween(long j, long j2) {
            final long j3 = j;
            final long j4 = j2;
            C04211 r0 = new Iterable<Pair<Long, Cue>>() {
                public Iterator<Pair<Long, Cue>> iterator() {
                    if (CueList.this.DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("slice (");
                        sb.append(j3);
                        sb.append(", ");
                        sb.append(j4);
                        sb.append("]=");
                        Log.d(CueList.TAG, sb.toString());
                    }
                    try {
                        return new EntryIterator(CueList.this.mCues.subMap(Long.valueOf(j3 + 1), Long.valueOf(j4 + 1)));
                    } catch (IllegalArgumentException unused) {
                        return new EntryIterator(null);
                    }
                }
            };
            return r0;
        }

        public long nextTimeAfter(long j) {
            try {
                SortedMap tailMap = this.mCues.tailMap(Long.valueOf(j + 1));
                if (tailMap != null) {
                    return ((Long) tailMap.firstKey()).longValue();
                }
            } catch (IllegalArgumentException | NoSuchElementException unused) {
            }
            return -1;
        }

        CueList() {
        }
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$RenderingWidget */
    public interface RenderingWidget {

        /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$RenderingWidget$OnChangedListener */
        public interface OnChangedListener {
            void onChanged(RenderingWidget renderingWidget);
        }

        void draw(Canvas canvas);

        void onAttachedToWindow();

        void onDetachedFromWindow();

        void setOnChangedListener(OnChangedListener onChangedListener);

        void setSize(int i, int i2);

        void setVisible(boolean z);
    }

    /* renamed from: android.support.v4.media.subtitle.SubtitleTrack$Run */
    private static class Run {
        public long mEndTimeMs = -1;
        public Cue mFirstCue;
        public Run mNextRunAtEndTimeMs;
        public Run mPrevRunAtEndTimeMs;
        public long mRunID = 0;
        private long mStoredEndTimeMs = -1;

        private Run() {
        }

        public void storeByEndTimeMs(LongSparseArray<Run> longSparseArray) {
            int indexOfKey = longSparseArray.indexOfKey(this.mStoredEndTimeMs);
            if (indexOfKey >= 0) {
                if (this.mPrevRunAtEndTimeMs == null) {
                    Run run = this.mNextRunAtEndTimeMs;
                    if (run == null) {
                        longSparseArray.removeAt(indexOfKey);
                    } else {
                        longSparseArray.setValueAt(indexOfKey, run);
                    }
                }
                removeAtEndTimeMs();
            }
            long j = this.mEndTimeMs;
            if (j >= 0) {
                this.mPrevRunAtEndTimeMs = null;
                this.mNextRunAtEndTimeMs = (Run) longSparseArray.get(j);
                Run run2 = this.mNextRunAtEndTimeMs;
                if (run2 != null) {
                    run2.mPrevRunAtEndTimeMs = this;
                }
                longSparseArray.put(this.mEndTimeMs, this);
                this.mStoredEndTimeMs = this.mEndTimeMs;
            }
        }

        public void removeAtEndTimeMs() {
            Run run = this.mPrevRunAtEndTimeMs;
            if (run != null) {
                run.mNextRunAtEndTimeMs = this.mNextRunAtEndTimeMs;
                this.mPrevRunAtEndTimeMs = null;
            }
            Run run2 = this.mNextRunAtEndTimeMs;
            if (run2 != null) {
                run2.mPrevRunAtEndTimeMs = run;
                this.mNextRunAtEndTimeMs = null;
            }
        }
    }

    public abstract RenderingWidget getRenderingWidget();

    /* access modifiers changed from: protected */
    public abstract void onData(byte[] bArr, boolean z, long j);

    public abstract void updateView(ArrayList<Cue> arrayList);

    public SubtitleTrack(MediaFormat mediaFormat) {
        this.mFormat = mediaFormat;
        this.mCues = new CueList();
        clearActiveCues();
        this.mLastTimeMs = -1;
    }

    public final MediaFormat getFormat() {
        return this.mFormat;
    }

    public void onData(SubtitleData2 subtitleData2) {
        long startTimeUs = subtitleData2.getStartTimeUs() + 1;
        onData(subtitleData2.getData(), true, startTimeUs);
        setRunDiscardTimeMs(startTimeUs, (subtitleData2.getStartTimeUs() + subtitleData2.getDurationUs()) / 1000);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0007, code lost:
        if (r6.mLastUpdateTimeMs > r8) goto L_0x0009;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void updateActiveCues(boolean r7, long r8) {
        /*
            r6 = this;
            monitor-enter(r6)
            if (r7 != 0) goto L_0x0009
            long r0 = r6.mLastUpdateTimeMs     // Catch:{ all -> 0x00ba }
            int r7 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r7 <= 0) goto L_0x000c
        L_0x0009:
            r6.clearActiveCues()     // Catch:{ all -> 0x00ba }
        L_0x000c:
            android.support.v4.media.subtitle.SubtitleTrack$CueList r7 = r6.mCues     // Catch:{ all -> 0x00ba }
            long r0 = r6.mLastUpdateTimeMs     // Catch:{ all -> 0x00ba }
            java.lang.Iterable r7 = r7.entriesBetween(r0, r8)     // Catch:{ all -> 0x00ba }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ all -> 0x00ba }
        L_0x0018:
            boolean r0 = r7.hasNext()     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x009f
            java.lang.Object r0 = r7.next()     // Catch:{ all -> 0x00ba }
            android.util.Pair r0 = (android.util.Pair) r0     // Catch:{ all -> 0x00ba }
            java.lang.Object r1 = r0.second     // Catch:{ all -> 0x00ba }
            android.support.v4.media.subtitle.SubtitleTrack$Cue r1 = (android.support.p000v4.media.subtitle.SubtitleTrack.Cue) r1     // Catch:{ all -> 0x00ba }
            long r2 = r1.mEndTimeMs     // Catch:{ all -> 0x00ba }
            java.lang.Object r4 = r0.first     // Catch:{ all -> 0x00ba }
            java.lang.Long r4 = (java.lang.Long) r4     // Catch:{ all -> 0x00ba }
            long r4 = r4.longValue()     // Catch:{ all -> 0x00ba }
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 != 0) goto L_0x0061
            boolean r0 = r6.DEBUG     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x0050
            java.lang.String r0 = "SubtitleTrack"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ba }
            r2.<init>()     // Catch:{ all -> 0x00ba }
            java.lang.String r3 = "Removing "
            r2.append(r3)     // Catch:{ all -> 0x00ba }
            r2.append(r1)     // Catch:{ all -> 0x00ba }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00ba }
            android.util.Log.v(r0, r2)     // Catch:{ all -> 0x00ba }
        L_0x0050:
            java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue> r0 = r6.mActiveCues     // Catch:{ all -> 0x00ba }
            r0.remove(r1)     // Catch:{ all -> 0x00ba }
            long r0 = r1.mRunID     // Catch:{ all -> 0x00ba }
            r2 = 0
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 != 0) goto L_0x0018
            r7.remove()     // Catch:{ all -> 0x00ba }
            goto L_0x0018
        L_0x0061:
            long r2 = r1.mStartTimeMs     // Catch:{ all -> 0x00ba }
            java.lang.Object r0 = r0.first     // Catch:{ all -> 0x00ba }
            java.lang.Long r0 = (java.lang.Long) r0     // Catch:{ all -> 0x00ba }
            long r4 = r0.longValue()     // Catch:{ all -> 0x00ba }
            int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r0 != 0) goto L_0x0096
            boolean r0 = r6.DEBUG     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x0089
            java.lang.String r0 = "SubtitleTrack"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ba }
            r2.<init>()     // Catch:{ all -> 0x00ba }
            java.lang.String r3 = "Adding "
            r2.append(r3)     // Catch:{ all -> 0x00ba }
            r2.append(r1)     // Catch:{ all -> 0x00ba }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00ba }
            android.util.Log.v(r0, r2)     // Catch:{ all -> 0x00ba }
        L_0x0089:
            long[] r0 = r1.mInnerTimesMs     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x0090
            r1.onTime(r8)     // Catch:{ all -> 0x00ba }
        L_0x0090:
            java.util.ArrayList<android.support.v4.media.subtitle.SubtitleTrack$Cue> r0 = r6.mActiveCues     // Catch:{ all -> 0x00ba }
            r0.add(r1)     // Catch:{ all -> 0x00ba }
            goto L_0x0018
        L_0x0096:
            long[] r0 = r1.mInnerTimesMs     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x0018
            r1.onTime(r8)     // Catch:{ all -> 0x00ba }
            goto L_0x0018
        L_0x009f:
            android.util.LongSparseArray<android.support.v4.media.subtitle.SubtitleTrack$Run> r7 = r6.mRunsByEndTime     // Catch:{ all -> 0x00ba }
            int r7 = r7.size()     // Catch:{ all -> 0x00ba }
            if (r7 <= 0) goto L_0x00b6
            android.util.LongSparseArray<android.support.v4.media.subtitle.SubtitleTrack$Run> r7 = r6.mRunsByEndTime     // Catch:{ all -> 0x00ba }
            r0 = 0
            long r1 = r7.keyAt(r0)     // Catch:{ all -> 0x00ba }
            int r7 = (r1 > r8 ? 1 : (r1 == r8 ? 0 : -1))
            if (r7 > 0) goto L_0x00b6
            r6.removeRunsByEndTimeIndex(r0)     // Catch:{ all -> 0x00ba }
            goto L_0x009f
        L_0x00b6:
            r6.mLastUpdateTimeMs = r8     // Catch:{ all -> 0x00ba }
            monitor-exit(r6)
            return
        L_0x00ba:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.SubtitleTrack.updateActiveCues(boolean, long):void");
    }

    private void removeRunsByEndTimeIndex(int i) {
        Run run = (Run) this.mRunsByEndTime.valueAt(i);
        while (run != null) {
            Cue cue = run.mFirstCue;
            while (cue != null) {
                this.mCues.remove(cue);
                Cue cue2 = cue.mNextInRun;
                cue.mNextInRun = null;
                cue = cue2;
            }
            this.mRunsByID.remove(run.mRunID);
            Run run2 = run.mNextRunAtEndTimeMs;
            run.mPrevRunAtEndTimeMs = null;
            run.mNextRunAtEndTimeMs = null;
            run = run2;
        }
        this.mRunsByEndTime.removeAt(i);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        for (int size = this.mRunsByEndTime.size() - 1; size >= 0; size--) {
            removeRunsByEndTimeIndex(size);
        }
        super.finalize();
    }

    private synchronized void takeTime(long j) {
        this.mLastTimeMs = j;
    }

    /* access modifiers changed from: protected */
    public synchronized void clearActiveCues() {
        if (this.DEBUG) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Clearing ");
            sb.append(this.mActiveCues.size());
            sb.append(" active cues");
            Log.v(str, sb.toString());
        }
        this.mActiveCues.clear();
        this.mLastUpdateTimeMs = -1;
    }

    /* access modifiers changed from: protected */
    public void scheduleTimedEvents() {
        if (this.mTimeProvider != null) {
            this.mNextScheduledTimeMs = this.mCues.nextTimeAfter(this.mLastTimeMs);
            if (this.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("sched @");
                sb.append(this.mNextScheduledTimeMs);
                sb.append(" after ");
                sb.append(this.mLastTimeMs);
                Log.d(TAG, sb.toString());
            }
            MediaTimeProvider mediaTimeProvider = this.mTimeProvider;
            long j = this.mNextScheduledTimeMs;
            mediaTimeProvider.notifyAt(j >= 0 ? j * 1000 : -1, this);
        }
    }

    public void onTimedEvent(long j) {
        if (this.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onTimedEvent ");
            sb.append(j);
            Log.d(TAG, sb.toString());
        }
        synchronized (this) {
            long j2 = j / 1000;
            updateActiveCues(false, j2);
            takeTime(j2);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    public void onSeek(long j) {
        if (this.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onSeek ");
            sb.append(j);
            Log.d(TAG, sb.toString());
        }
        synchronized (this) {
            long j2 = j / 1000;
            updateActiveCues(true, j2);
            takeTime(j2);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    public void onStop() {
        synchronized (this) {
            if (this.DEBUG) {
                Log.d(TAG, "onStop");
            }
            clearActiveCues();
            this.mLastTimeMs = -1;
        }
        updateView(this.mActiveCues);
        this.mNextScheduledTimeMs = -1;
        this.mTimeProvider.notifyAt(-1, this);
    }

    public void show() {
        if (!this.mVisible) {
            this.mVisible = true;
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(true);
            }
            MediaTimeProvider mediaTimeProvider = this.mTimeProvider;
            if (mediaTimeProvider != null) {
                mediaTimeProvider.scheduleUpdate(this);
            }
        }
    }

    public void hide() {
        if (this.mVisible) {
            MediaTimeProvider mediaTimeProvider = this.mTimeProvider;
            if (mediaTimeProvider != null) {
                mediaTimeProvider.cancelNotifications(this);
            }
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(false);
            }
            this.mVisible = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setTimeProvider(android.support.p000v4.media.subtitle.MediaTimeProvider r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            android.support.v4.media.subtitle.MediaTimeProvider r0 = r1.mTimeProvider     // Catch:{ all -> 0x001d }
            if (r0 != r2) goto L_0x0007
            monitor-exit(r1)
            return
        L_0x0007:
            android.support.v4.media.subtitle.MediaTimeProvider r0 = r1.mTimeProvider     // Catch:{ all -> 0x001d }
            if (r0 == 0) goto L_0x0010
            android.support.v4.media.subtitle.MediaTimeProvider r0 = r1.mTimeProvider     // Catch:{ all -> 0x001d }
            r0.cancelNotifications(r1)     // Catch:{ all -> 0x001d }
        L_0x0010:
            r1.mTimeProvider = r2     // Catch:{ all -> 0x001d }
            android.support.v4.media.subtitle.MediaTimeProvider r2 = r1.mTimeProvider     // Catch:{ all -> 0x001d }
            if (r2 == 0) goto L_0x001b
            android.support.v4.media.subtitle.MediaTimeProvider r2 = r1.mTimeProvider     // Catch:{ all -> 0x001d }
            r2.scheduleUpdate(r1)     // Catch:{ all -> 0x001d }
        L_0x001b:
            monitor-exit(r1)
            return
        L_0x001d:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.SubtitleTrack.setTimeProvider(android.support.v4.media.subtitle.MediaTimeProvider):void");
    }

    public void setRunDiscardTimeMs(long j, long j2) {
        if (j != 0 && j != -1) {
            Run run = (Run) this.mRunsByID.get(j);
            if (run != null) {
                run.mEndTimeMs = j2;
                run.storeByEndTimeMs(this.mRunsByEndTime);
            }
        }
    }

    public int getTrackType() {
        return getRenderingWidget() == null ? 3 : 4;
    }
}
