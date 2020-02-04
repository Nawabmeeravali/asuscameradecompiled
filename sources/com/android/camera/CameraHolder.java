package com.android.camera;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.camera.CameraManager.CameraOpenErrorCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.util.CameraUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraHolder {
    private static final boolean DEBUG_OPEN_RELEASE = true;
    private static final int KEEP_CAMERA_TIMEOUT = 3000;
    private static final int RELEASE_CAMERA = 1;
    private static final String TAG = "CameraHolder";
    private static boolean mCam2On = false;
    private static Context mContext;
    private static CameraProxy[] mMockCamera;
    private static CameraInfo[] mMockCameraInfo;
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static CameraHolder sHolder;
    private static ArrayList<OpenReleaseState> sOpenReleaseStates = new ArrayList<>();
    private int mBackCameraId = -1;
    private CameraProxy mCameraDevice;
    private int mCameraId = -1;
    /* access modifiers changed from: private */
    public boolean mCameraOpened;
    private ArrayList<CameraCharacteristics> mCharacteristics = new ArrayList<>();
    private int mFrontCameraId = -1;
    private final Handler mHandler;
    private CameraInfo[] mInfo;
    private long mKeepBeforeTime;
    private final int mNumberOfCameras;
    private Parameters mParameters;

    public class CameraInfo {
        public static final int CAMERA_FACING_BACK = 1;
        public static final int CAMERA_FACING_FRONT = 0;
        public int facing;
        public int orientation;

        public CameraInfo() {
        }
    }

    private class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                synchronized (CameraHolder.this) {
                    if (!CameraHolder.this.mCameraOpened) {
                        CameraHolder.this.release();
                    }
                }
            }
        }
    }

    private static class OpenReleaseState {
        String device;

        /* renamed from: id */
        int f71id;
        String[] stack;
        long time;

        private OpenReleaseState() {
        }
    }

    private static synchronized void collectState(int i, CameraProxy cameraProxy) {
        synchronized (CameraHolder.class) {
            OpenReleaseState openReleaseState = new OpenReleaseState();
            openReleaseState.time = System.currentTimeMillis();
            openReleaseState.f71id = i;
            if (cameraProxy == null) {
                openReleaseState.device = "(null)";
            } else {
                openReleaseState.device = cameraProxy.toString();
            }
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String[] strArr = new String[stackTrace.length];
            for (int i2 = 0; i2 < stackTrace.length; i2++) {
                strArr[i2] = stackTrace[i2].toString();
            }
            openReleaseState.stack = strArr;
            if (sOpenReleaseStates.size() > 10) {
                sOpenReleaseStates.remove(0);
            }
            sOpenReleaseStates.add(openReleaseState);
        }
    }

    private static synchronized void dumpStates() {
        synchronized (CameraHolder.class) {
            for (int size = sOpenReleaseStates.size() - 1; size >= 0; size--) {
                OpenReleaseState openReleaseState = (OpenReleaseState) sOpenReleaseStates.get(size);
                String format = sDateFormat.format(new Date(openReleaseState.time));
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("State ");
                sb.append(size);
                sb.append(" at ");
                sb.append(format);
                Log.d(str, sb.toString());
                String str2 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("mCameraId = ");
                sb2.append(openReleaseState.f71id);
                sb2.append(", mCameraDevice = ");
                sb2.append(openReleaseState.device);
                Log.d(str2, sb2.toString());
                Log.d(TAG, "Stack:");
                for (String append : openReleaseState.stack) {
                    String str3 = TAG;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("  ");
                    sb3.append(append);
                    Log.d(str3, sb3.toString());
                }
            }
        }
    }

    public static synchronized CameraHolder instance() {
        CameraHolder cameraHolder;
        synchronized (CameraHolder.class) {
            if (sHolder == null) {
                sHolder = new CameraHolder();
            }
            cameraHolder = sHolder;
        }
        return cameraHolder;
    }

    public static void setCamera2Mode(Context context, boolean z) {
        mContext = context;
        mCam2On = z;
    }

    public static void injectMockCamera(CameraInfo[] cameraInfoArr, CameraProxy[] cameraProxyArr) {
        mMockCameraInfo = cameraInfoArr;
        mMockCamera = cameraProxyArr;
        sHolder = new CameraHolder();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0086  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private CameraHolder() {
        /*
            r9 = this;
            r9.<init>()
            r0 = -1
            r9.mCameraId = r0
            r9.mBackCameraId = r0
            r9.mFrontCameraId = r0
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r9.mCharacteristics = r1
            android.os.HandlerThread r1 = new android.os.HandlerThread
            java.lang.String r2 = "CameraHolder"
            r1.<init>(r2)
            r1.start()
            com.android.camera.CameraHolder$MyHandler r3 = new com.android.camera.CameraHolder$MyHandler
            android.os.Looper r1 = r1.getLooper()
            r3.<init>(r1)
            r9.mHandler = r3
            android.content.Context r1 = mContext
            java.lang.String r3 = "camera"
            java.lang.Object r1 = r1.getSystemService(r3)
            android.hardware.camera2.CameraManager r1 = (android.hardware.camera2.CameraManager) r1
            r3 = 0
            java.lang.String[] r4 = r1.getCameraIdList()     // Catch:{ CameraAccessException -> 0x0076 }
            int r5 = r4.length     // Catch:{ CameraAccessException -> 0x0074 }
            com.android.camera.CameraHolder$CameraInfo[] r5 = new com.android.camera.CameraHolder.CameraInfo[r5]     // Catch:{ CameraAccessException -> 0x0074 }
            r9.mInfo = r5     // Catch:{ CameraAccessException -> 0x0074 }
            r5 = r3
        L_0x003b:
            int r6 = r4.length     // Catch:{ CameraAccessException -> 0x0074 }
            if (r5 >= r6) goto L_0x007b
            r6 = r4[r5]     // Catch:{ CameraAccessException -> 0x0074 }
            android.hardware.camera2.CameraCharacteristics r6 = r1.getCameraCharacteristics(r6)     // Catch:{ CameraAccessException -> 0x0074 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ CameraAccessException -> 0x0074 }
            r7.<init>()     // Catch:{ CameraAccessException -> 0x0074 }
            java.lang.String r8 = "cameraIdList size ="
            r7.append(r8)     // Catch:{ CameraAccessException -> 0x0074 }
            int r8 = r4.length     // Catch:{ CameraAccessException -> 0x0074 }
            r7.append(r8)     // Catch:{ CameraAccessException -> 0x0074 }
            java.lang.String r7 = r7.toString()     // Catch:{ CameraAccessException -> 0x0074 }
            android.util.Log.d(r2, r7)     // Catch:{ CameraAccessException -> 0x0074 }
            android.hardware.camera2.CameraCharacteristics$Key r7 = android.hardware.camera2.CameraCharacteristics.LENS_FACING     // Catch:{ CameraAccessException -> 0x0074 }
            java.lang.Object r7 = r6.get(r7)     // Catch:{ CameraAccessException -> 0x0074 }
            java.lang.Integer r7 = (java.lang.Integer) r7     // Catch:{ CameraAccessException -> 0x0074 }
            int r7 = r7.intValue()     // Catch:{ CameraAccessException -> 0x0074 }
            if (r7 != 0) goto L_0x0069
            com.android.camera.CaptureModule.FRONT_ID = r5     // Catch:{ CameraAccessException -> 0x0074 }
        L_0x0069:
            r9.addCameraInfo(r5, r6)     // Catch:{ CameraAccessException -> 0x0074 }
            java.util.ArrayList<android.hardware.camera2.CameraCharacteristics> r7 = r9.mCharacteristics     // Catch:{ CameraAccessException -> 0x0074 }
            r7.add(r5, r6)     // Catch:{ CameraAccessException -> 0x0074 }
            int r5 = r5 + 1
            goto L_0x003b
        L_0x0074:
            r1 = move-exception
            goto L_0x0078
        L_0x0076:
            r1 = move-exception
            r4 = 0
        L_0x0078:
            r1.printStackTrace()
        L_0x007b:
            if (r4 != 0) goto L_0x007f
            r1 = r3
            goto L_0x0080
        L_0x007f:
            int r1 = r4.length
        L_0x0080:
            r9.mNumberOfCameras = r1
        L_0x0082:
            int r1 = r9.mNumberOfCameras
            if (r3 >= r1) goto L_0x00a7
            int r1 = r9.mBackCameraId
            if (r1 != r0) goto L_0x0096
            com.android.camera.CameraHolder$CameraInfo[] r1 = r9.mInfo
            r1 = r1[r3]
            int r1 = r1.facing
            r2 = 1
            if (r1 != r2) goto L_0x0096
            r9.mBackCameraId = r3
            goto L_0x00a4
        L_0x0096:
            int r1 = r9.mFrontCameraId
            if (r1 != r0) goto L_0x00a4
            com.android.camera.CameraHolder$CameraInfo[] r1 = r9.mInfo
            r1 = r1[r3]
            int r1 = r1.facing
            if (r1 != 0) goto L_0x00a4
            r9.mFrontCameraId = r3
        L_0x00a4:
            int r3 = r3 + 1
            goto L_0x0082
        L_0x00a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.CameraHolder.<init>():void");
    }

    private void addCameraInfo(int i, CameraCharacteristics cameraCharacteristics) {
        this.mInfo[i] = new CameraInfo();
        this.mInfo[i].facing = ((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
        this.mInfo[i].orientation = ((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
    }

    public CameraCharacteristics getCameraCharacteristics(int i) {
        return (CameraCharacteristics) this.mCharacteristics.get(i);
    }

    public int getNumberOfCameras() {
        return this.mNumberOfCameras;
    }

    public CameraInfo[] getCameraInfo() {
        return this.mInfo;
    }

    public synchronized CameraProxy open(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback) {
        collectState(i, this.mCameraDevice);
        if (this.mCameraOpened) {
            Log.e(TAG, "double open");
            dumpStates();
        }
        CameraUtil.Assert(!this.mCameraOpened);
        if (!(this.mCameraDevice == null || this.mCameraId == i)) {
            this.mCameraDevice.release();
            this.mCameraDevice = null;
            this.mCameraId = -1;
        }
        if (this.mCameraDevice == null) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("open camera ");
            sb.append(i);
            Log.v(str, sb.toString());
            if (mMockCameraInfo == null) {
                this.mCameraDevice = CameraManagerFactory.getAndroidCameraManager().cameraOpen(handler, i, cameraOpenErrorCallback);
            } else if (mMockCamera != null) {
                this.mCameraDevice = mMockCamera[i];
            } else {
                Log.e(TAG, "MockCameraInfo found, but no MockCamera provided.");
                this.mCameraDevice = null;
            }
            if (this.mCameraDevice == null) {
                String str2 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("fail to connect Camera:");
                sb2.append(this.mCameraId);
                sb2.append(", aborting.");
                Log.e(str2, sb2.toString());
                return null;
            }
            this.mCameraId = i;
            this.mParameters = this.mCameraDevice.getCamera().getParameters();
        } else if (!this.mCameraDevice.reconnect(handler, cameraOpenErrorCallback)) {
            String str3 = TAG;
            StringBuilder sb3 = new StringBuilder();
            sb3.append("fail to reconnect Camera:");
            sb3.append(this.mCameraId);
            sb3.append(", aborting.");
            Log.e(str3, sb3.toString());
            return null;
        } else {
            this.mCameraDevice.setParameters(this.mParameters);
        }
        this.mCameraOpened = DEBUG_OPEN_RELEASE;
        this.mHandler.removeMessages(1);
        this.mKeepBeforeTime = 0;
        return this.mCameraDevice;
    }

    public synchronized CameraProxy tryOpen(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback) {
        return !this.mCameraOpened ? open(handler, i, cameraOpenErrorCallback) : null;
    }

    public synchronized void release() {
        collectState(this.mCameraId, this.mCameraDevice);
        if (this.mCameraDevice != null) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis < this.mKeepBeforeTime) {
                if (this.mCameraOpened) {
                    this.mCameraOpened = false;
                    this.mCameraDevice.stopPreview();
                }
                this.mHandler.sendEmptyMessageDelayed(1, this.mKeepBeforeTime - currentTimeMillis);
                return;
            }
            strongRelease();
        }
    }

    public synchronized void strongRelease() {
        if (this.mCameraDevice != null) {
            this.mCameraOpened = false;
            this.mCameraDevice.release();
            this.mCameraDevice = null;
            this.mParameters = null;
            this.mCameraId = -1;
        }
    }

    public void keep() {
        keep(KEEP_CAMERA_TIMEOUT);
    }

    public synchronized void keep(int i) {
        this.mKeepBeforeTime = System.currentTimeMillis() + ((long) i);
    }

    public int getBackCameraId() {
        return this.mBackCameraId;
    }

    public int getFrontCameraId() {
        return this.mFrontCameraId;
    }
}
