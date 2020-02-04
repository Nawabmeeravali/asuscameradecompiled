package com.android.camera.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import com.android.camera.CameraActivity;
import com.android.camera.CameraDisabledException;
import com.android.camera.CameraHolder;
import com.android.camera.CameraManager.CameraOpenErrorCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraSettings;
import com.android.camera.p004ui.RotateTextToast;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.codeaurora.snapcam.C0905R;

public class CameraUtil {
    public static final String ACTION_CAMERA_SHUTTER_CLICK = "com.android.camera.action.SHUTTER_CLICK";
    public static final String ACTION_CAMERA_STARTED = "com.android.camera.action.CAMERA_STARTED";
    public static final String ACTION_CAMERA_STOPPED = "com.android.camera.action.CAMERA_STOPPED";
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";
    public static final String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";
    private static final String AUTO_EXPOSURE_LOCK_SUPPORTED = "auto-exposure-lock-supported";
    private static final String AUTO_HDR_SUPPORTED = "auto-hdr-supported";
    private static final String AUTO_WHITE_BALANCE_LOCK_SUPPORTED = "auto-whitebalance-lock-supported";
    private static final String DIALOG_CONFIG = "dialog_config";
    private static final String EXTRAS_CAMERA_FACING = "android.intent.extras.CAMERA_FACING";
    public static final String FALSE = "false";
    public static final String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String KEY_DELETE = "delete";
    public static final String KEY_DELETE_ALL = "delete_all";
    public static final String KEY_IS_SECURE_CAMERA = "is_secure_camera";
    public static final String KEY_RETURN_DATA = "return-data";
    public static final String KEY_SAVE = "save";
    public static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";
    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final int MAX_PREVIEW_FPS_TIMES_1000 = 400000;
    public static final int MODE_ONE_BT = 0;
    public static final int MODE_TWO_BT = 1;
    public static final int ORIENTATION_HYSTERESIS = 5;
    private static final int PREFERRED_PREVIEW_FPS_TIMES_1000 = 30000;
    public static final int RATIO_16_9 = 1;
    public static final int RATIO_3_2 = 3;
    public static final int RATIO_4_3 = 2;
    public static final int RATIO_UNKNOWN = 0;
    public static final String RECORDING_HINT = "recording-hint";
    public static final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
    public static final String SCENE_MODE_HDR = "hdr";
    public static final String SECURE_CAMERA_EXTRA = "secure_camera";
    private static final String TAG = "Util";
    public static final String TRUE = "true";
    private static final String VIDEO_SNAPSHOT_SUPPORTED = "video-snapshot-supported";
    private static ImageFileNamer sImageFileNamer = null;
    private static int[] sLocation = new int[2];
    private static float sPixelDensity = 1.0f;

    public static class CompareSizesByArea implements Comparator<Size> {
        public int compare(Size size, Size size2) {
            return Long.signum((((long) size.getWidth()) * ((long) size.getHeight())) - (((long) size2.getWidth()) * ((long) size2.getHeight())));
        }
    }

    private static class ImageFileNamer {
        private final int REFOCUS_ALLFOCUS_IDX = 6;
        private final String REFOCUS_ALLFOCUS_SUFFIX = "Allfocus";
        private final int REFOCUS_DEPTHMAP_IDX = 5;
        private final String REFOCUS_DEPTHMAP_SUFFIX = "DepthMap";
        private final SimpleDateFormat mFormat;
        private long mLastDate;
        private int mRefocusIdx = 0;
        private int mSameSecondCount;

        public ImageFileNamer(String str) {
            this.mFormat = new SimpleDateFormat(str);
        }

        public String generateName(long j, boolean z) {
            String format = this.mFormat.format(new Date(j));
            String str = "_";
            if (z) {
                int i = this.mRefocusIdx;
                if (i == 5) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(format);
                    sb.append("_DepthMap");
                    String sb2 = sb.toString();
                    this.mRefocusIdx++;
                    return sb2;
                } else if (i == 6) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(format);
                    sb3.append("_Allfocus");
                    String sb4 = sb3.toString();
                    this.mRefocusIdx = 0;
                    return sb4;
                } else {
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append(format);
                    sb5.append(str);
                    sb5.append(this.mRefocusIdx);
                    String sb6 = sb5.toString();
                    this.mRefocusIdx++;
                    return sb6;
                }
            } else if (j / 1000 == this.mLastDate / 1000) {
                this.mSameSecondCount++;
                StringBuilder sb7 = new StringBuilder();
                sb7.append(format);
                sb7.append(str);
                sb7.append(this.mSameSecondCount);
                return sb7.toString();
            } else {
                this.mLastDate = j;
                this.mSameSecondCount = 0;
                return format;
            }
        }
    }

    public static float clamp(float f, float f2, float f3) {
        return f > f3 ? f3 : f < f2 ? f2 : f;
    }

    public static int clamp(int i, int i2, int i3) {
        return i > i3 ? i3 : i < i2 ? i2 : i;
    }

    public static String convertOutputFormatToFileExt(int i) {
        return i == 2 ? ".mp4" : ".3gp";
    }

    public static String convertOutputFormatToMimeType(int i) {
        return i == 2 ? "video/mp4" : "video/3gpp";
    }

    public static int determinCloseRatio(float f) {
        int i;
        if (((double) f) == 1.0d) {
            return 0;
        }
        if (f < 1.0f) {
            f = 1.0f / f;
        }
        float f2 = 1.3333334f / f;
        if (f2 < 1.0f) {
            f2 = 1.0f / f2;
        }
        float f3 = 1.7777778f / f;
        if (f3 < 1.0f) {
            f3 = 1.0f / f3;
        }
        float f4 = 1.5f / f;
        float f5 = f4 < 1.0f ? 1.0f / f4 : f4;
        if (f5 < f2) {
            i = 3;
        } else {
            i = 2;
            f5 = f2;
        }
        if (f5 > f3) {
            return 1;
        }
        return i;
    }

    public static int determineRatio(float f) {
        if (f < 1.0f) {
            f = 1.0f / f;
        }
        if (f > 1.33f && f < 1.34f) {
            return 2;
        }
        if (f <= 1.77f || f >= 1.78f) {
            return (f <= 1.49f || f >= 1.51f) ? 0 : 3;
        }
        return 1;
    }

    public static String getFilpModeString(int i) {
        if (i == 0) {
            return "off";
        }
        if (i == 1) {
            return CameraSettings.FLIP_MODE_H;
        }
        if (i == 2) {
            return CameraSettings.FLIP_MODE_V;
        }
        if (i != 3) {
            return null;
        }
        return CameraSettings.FLIP_MODE_VH;
    }

    private static boolean isBackCameraIntent(int i) {
        return i == 0;
    }

    private static boolean isFrontCameraIntent(int i) {
        return i == 1;
    }

    public static int nextPowerOf2(int i) {
        int i2 = i - 1;
        int i3 = i2 | (i2 >>> 16);
        int i4 = i3 | (i3 >>> 8);
        int i5 = i4 | (i4 >>> 4);
        int i6 = i5 | (i5 >>> 2);
        return (i6 | (i6 >>> 1)) + 1;
    }

    public static boolean isSupported(String str, List<String> list) {
        return list != null && list.indexOf(str) >= 0;
    }

    public static boolean isAutoExposureLockSupported(Parameters parameters) {
        return TRUE.equals(parameters.get(AUTO_EXPOSURE_LOCK_SUPPORTED));
    }

    public static boolean isAutoHDRSupported(Parameters parameters) {
        return TRUE.equals(parameters.get(AUTO_HDR_SUPPORTED));
    }

    public static boolean isAutoWhiteBalanceLockSupported(Parameters parameters) {
        return TRUE.equals(parameters.get(AUTO_WHITE_BALANCE_LOCK_SUPPORTED));
    }

    public static boolean isVideoSnapshotSupported(Parameters parameters) {
        if (parameters == null) {
            return false;
        }
        return TRUE.equals(parameters.get(VIDEO_SNAPSHOT_SUPPORTED));
    }

    public static boolean isCameraHdrSupported(Parameters parameters) {
        List supportedSceneModes = parameters.getSupportedSceneModes();
        return supportedSceneModes != null && supportedSceneModes.contains(SCENE_MODE_HDR);
    }

    public static boolean isMeteringAreaSupported(Parameters parameters) {
        return parameters.getMaxNumMeteringAreas() > 0;
    }

    public static boolean isFocusAreaSupported(Parameters parameters) {
        if (parameters.getMaxNumFocusAreas() > 0) {
            if (isSupported("auto", parameters.getSupportedFocusModes())) {
                return true;
            }
        }
        return false;
    }

    private CameraUtil() {
    }

    public static void initialize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        sPixelDensity = displayMetrics.density;
        sImageFileNamer = new ImageFileNamer(context.getString(C0905R.string.image_file_name_format));
    }

    public static int dpToPixel(int i) {
        return Math.round(sPixelDensity * ((float) i));
    }

    public static Bitmap rotate(Bitmap bitmap, int i) {
        return rotateAndMirror(bitmap, i, false);
    }

    public static Bitmap rotateAndMirror(Bitmap bitmap, int i, boolean z) {
        if ((i == 0 && !z) || bitmap == null) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        if (z) {
            matrix.postScale(-1.0f, 1.0f);
            i = (i + 360) % 360;
            if (i == 0 || i == 180) {
                matrix.postTranslate((float) bitmap.getWidth(), 0.0f);
            } else if (i == 90 || i == 270) {
                matrix.postTranslate((float) bitmap.getHeight(), 0.0f);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid degrees=");
                sb.append(i);
                throw new IllegalArgumentException(sb.toString());
            }
        }
        if (i != 0) {
            matrix.postRotate((float) i, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap == createBitmap) {
                return bitmap;
            }
            bitmap.recycle();
            return createBitmap;
        } catch (OutOfMemoryError unused) {
            return bitmap;
        }
    }

    public static int computeSampleSize(Options options, int i, int i2) {
        int computeInitialSampleSize = computeInitialSampleSize(options, i, i2);
        if (computeInitialSampleSize > 8) {
            return 8 * ((computeInitialSampleSize + 7) / 8);
        }
        int i3 = 1;
        while (i3 < computeInitialSampleSize) {
            i3 <<= 1;
        }
        return i3;
    }

    private static int computeInitialSampleSize(Options options, int i, int i2) {
        int i3;
        int i4;
        double d = (double) options.outWidth;
        double d2 = (double) options.outHeight;
        if (i2 < 0) {
            i3 = 1;
        } else {
            i3 = (int) Math.ceil(Math.sqrt((d * d2) / ((double) i2)));
        }
        if (i < 0) {
            i4 = 128;
        } else {
            double d3 = (double) i;
            i4 = (int) Math.min(Math.floor(d / d3), Math.floor(d2 / d3));
        }
        if (i4 < i3) {
            return i3;
        }
        if (i2 >= 0 || i >= 0) {
            return i < 0 ? i3 : i4;
        }
        return 1;
    }

    public static Bitmap makeBitmap(byte[] bArr, int i) {
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
            if (!options.mCancel && options.outWidth != -1) {
                if (options.outHeight != -1) {
                    options.inSampleSize = computeSampleSize(options, -1, i);
                    options.inJustDecodeBounds = false;
                    options.inDither = false;
                    options.inPreferredConfig = Config.ARGB_8888;
                    return BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                }
            }
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Got oom exception ", e);
            return null;
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable unused) {
            }
        }
    }

    public static void Assert(boolean z) {
        if (!z) {
            throw new AssertionError();
        }
    }

    private static void throwIfCameraDisabled(Activity activity) throws CameraDisabledException {
        if (((DevicePolicyManager) activity.getSystemService("device_policy")).getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }
    }

    public static CameraProxy openCamera(Activity activity, final int i, Handler handler, final CameraOpenErrorCallback cameraOpenErrorCallback) {
        try {
            throwIfCameraDisabled(activity);
            return CameraHolder.instance().open(handler, i, cameraOpenErrorCallback);
        } catch (CameraDisabledException unused) {
            handler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallback.this.onCameraDisabled(i);
                }
            });
            return null;
        }
    }

    public static void showErrorAndFinish(final Activity activity, int i) {
        if (activity != null && !activity.isFinishing()) {
            C08982 r0 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.finish();
                }
            };
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(16843605, typedValue, true);
            new Builder(activity).setCancelable(false).setTitle(C0905R.string.camera_error_title).setMessage(i).setNeutralButton(C0905R.string.dialog_ok, r0).setIcon(typedValue.resourceId).show();
        }
    }

    public static <T> T checkNotNull(T t) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException();
    }

    public static boolean equals(Object obj, Object obj2) {
        return obj == obj2 || (obj != null && obj.equals(obj2));
    }

    public static float distance(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return (float) Math.sqrt((double) ((f5 * f5) + (f6 * f6)));
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == 0) {
            return 0;
        }
        if (rotation == 1) {
            return 90;
        }
        if (rotation != 2) {
            return rotation != 3 ? 0 : 270;
        }
        return 180;
    }

    public static boolean isDefaultToPortrait(Activity activity) {
        int i;
        int i2;
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int rotation = defaultDisplay.getRotation();
        if (rotation == 0 || rotation == 2) {
            i = point.x;
            i2 = point.y;
        } else {
            i = point.y;
            i2 = point.x;
        }
        return i < i2;
    }

    public static int getDisplayOrientationCamera2(int i, int i2) {
        CameraCharacteristics cameraCharacteristics = CameraHolder.instance().getCameraCharacteristics(i2);
        if (((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
            return (360 - ((((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() + i) % 360)) % 360;
        }
        return ((((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() - i) + 360) % 360;
    }

    public static int getDisplayOrientation(int i, int i2) {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(i2, cameraInfo);
        if (cameraInfo.facing == 1) {
            return (360 - ((cameraInfo.orientation + i) % 360)) % 360;
        }
        return ((cameraInfo.orientation - i) + 360) % 360;
    }

    public static int getCameraOrientation(int i) {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(i, cameraInfo);
        return cameraInfo.orientation;
    }

    public static int roundOrientation(int i, int i2) {
        boolean z = true;
        if (i2 != -1) {
            int abs = Math.abs(i - i2);
            if (Math.min(abs, 360 - abs) < 50) {
                z = false;
            }
        }
        return z ? (((i + 45) / 90) * 90) % 360 : i2;
    }

    private static Point getDefaultDisplaySize(Activity activity, Point point) {
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        String displayUMax = PersistUtil.getDisplayUMax();
        String displayLMax = PersistUtil.getDisplayLMax();
        if (displayUMax.length() > 0 && displayLMax.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("display uMax ");
            sb.append(displayUMax);
            sb.append(" lMax ");
            sb.append(displayLMax);
            String sb2 = sb.toString();
            String str = TAG;
            Log.v(str, sb2);
            String str2 = "x";
            String[] split = displayUMax.split(str2, 2);
            String[] split2 = displayLMax.split(str2, 2);
            try {
                int parseInt = Integer.parseInt(split[0]);
                int parseInt2 = Integer.parseInt(split[1]);
                int parseInt3 = Integer.parseInt(split2[0]);
                int parseInt4 = Integer.parseInt(split2[1]);
                int i = point.x * point.y;
                if (i > parseInt * parseInt2) {
                    point.set(parseInt, parseInt2);
                } else if (i >= parseInt3 * parseInt4) {
                    point.set(parseInt3, parseInt4);
                } else {
                    Log.v(str, "No need to cap display size");
                }
            } catch (Exception unused) {
                Log.e(str, "Invalid display properties");
            }
        }
        return point;
    }

    public static Camera.Size getOptimalPreviewSize(Activity activity, List<Camera.Size> list, double d) {
        Point[] pointArr = new Point[list.size()];
        int i = 0;
        for (Camera.Size size : list) {
            int i2 = i + 1;
            pointArr[i] = new Point(size.width, size.height);
            i = i2;
        }
        int optimalPreviewSize = getOptimalPreviewSize(activity, pointArr, d);
        if (optimalPreviewSize == -1) {
            return null;
        }
        return (Camera.Size) list.get(optimalPreviewSize);
    }

    public static int getOptimalPreviewSize(Activity activity, Point[] pointArr, double d) {
        Point[] pointArr2 = pointArr;
        if (pointArr2 == null) {
            return -1;
        }
        Point point = new Point();
        Activity activity2 = activity;
        getDefaultDisplaySize(activity, point);
        int min = Math.min(point.x, point.y);
        double d2 = Double.MAX_VALUE;
        int i = -1;
        double d3 = Double.MAX_VALUE;
        for (int i2 = 0; i2 < pointArr2.length; i2++) {
            Point point2 = pointArr2[i2];
            if (Math.abs((((double) point2.x) / ((double) point2.y)) - d) <= 0.01d) {
                int i3 = point2.y;
                if (i3 <= 1080) {
                    double abs = (double) Math.abs(i3 - min);
                    if (abs < d3) {
                        d3 = (double) Math.abs(point2.y - min);
                        i = i2;
                    } else if (abs == d3 && point2.y < min) {
                        i = i2;
                        d3 = abs;
                    }
                }
            }
        }
        if (i == -1) {
            Log.w(TAG, "No preview size match the aspect ratio");
            for (int i4 = 0; i4 < pointArr2.length; i4++) {
                Point point3 = pointArr2[i4];
                if (((double) Math.abs(point3.y - min)) < d2) {
                    d2 = (double) Math.abs(point3.y - min);
                    i = i4;
                }
            }
        }
        return i;
    }

    public static Camera.Size getOptimalVideoSnapshotPictureSize(List<Camera.Size> list, double d) {
        Camera.Size size = null;
        if (list == null) {
            return null;
        }
        for (Camera.Size size2 : list) {
            if (Math.abs((((double) size2.width) / ((double) size2.height)) - d) <= 0.001d && (size == null || size2.width > size.width)) {
                size = size2;
            }
        }
        if (size == null) {
            Log.w(TAG, "No picture size match the aspect ratio");
            for (Camera.Size size3 : list) {
                if (size == null || size3.width > size.width) {
                    size = size3;
                }
            }
        }
        return size;
    }

    public static Camera.Size getOptimalJpegThumbnailSize(List<Camera.Size> list, double d) {
        Camera.Size size = null;
        if (list == null) {
            return null;
        }
        for (Camera.Size size2 : list) {
            if (Math.abs((((double) size2.width) / ((double) size2.height)) - d) <= 0.001d && (size == null || size2.width > size.width)) {
                size = size2;
            }
        }
        if (size == null) {
            Log.w(TAG, "No thumbnail size match the aspect ratio");
            for (Camera.Size size3 : list) {
                if (size == null || size3.width > size.width) {
                    size = size3;
                }
            }
        }
        return size;
    }

    public static void dumpParameters(Parameters parameters) {
        StringTokenizer stringTokenizer = new StringTokenizer(parameters.flatten(), ";");
        String str = TAG;
        Log.d(str, "Dump all camera parameters:");
        while (stringTokenizer.hasMoreElements()) {
            Log.d(str, stringTokenizer.nextToken());
        }
    }

    public static boolean isMmsCapable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            return false;
        }
        try {
            return ((Boolean) TelephonyManager.class.getMethod("isVoiceCapable", new Class[0]).invoke(telephonyManager, new Object[0])).booleanValue();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            return true;
        }
    }

    public static int getCameraFacingIntentExtras(Activity activity) {
        int i;
        int intExtra = activity.getIntent().getIntExtra(EXTRAS_CAMERA_FACING, -1);
        if (isFrontCameraIntent(intExtra)) {
            i = CameraHolder.instance().getFrontCameraId();
            if (i == -1) {
                return -1;
            }
        } else if (!isBackCameraIntent(intExtra)) {
            return -1;
        } else {
            i = CameraHolder.instance().getBackCameraId();
            if (i == -1) {
                return -1;
            }
        }
        return i;
    }

    public static boolean pointInView(float f, float f2, View view) {
        view.getLocationInWindow(sLocation);
        int[] iArr = sLocation;
        if (f < ((float) iArr[0]) || f >= ((float) (iArr[0] + view.getWidth()))) {
            return false;
        }
        int[] iArr2 = sLocation;
        if (f2 < ((float) iArr2[1]) || f2 >= ((float) (iArr2[1] + view.getHeight()))) {
            return false;
        }
        return true;
    }

    public static int[] getRelativeLocation(View view, View view2) {
        view.getLocationInWindow(sLocation);
        int[] iArr = sLocation;
        int i = iArr[0];
        int i2 = iArr[1];
        view2.getLocationInWindow(iArr);
        int[] iArr2 = sLocation;
        iArr2[0] = iArr2[0] - i;
        iArr2[1] = iArr2[1] - i2;
        return iArr2;
    }

    public static boolean isUriValid(Uri uri, ContentResolver contentResolver) {
        if (uri == null) {
            return false;
        }
        try {
            ParcelFileDescriptor openFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (openFileDescriptor == null) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Fail to open URI. URI=");
                sb.append(uri);
                Log.e(str, sb.toString());
                return false;
            }
            openFileDescriptor.close();
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public static void dumpRect(RectF rectF, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("=(");
        sb.append(rectF.left);
        String str2 = ",";
        sb.append(str2);
        sb.append(rectF.top);
        sb.append(str2);
        sb.append(rectF.right);
        sb.append(str2);
        sb.append(rectF.bottom);
        sb.append(")");
        Log.v(TAG, sb.toString());
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static Rect rectFToRect(RectF rectF) {
        Rect rect = new Rect();
        rectFToRect(rectF, rect);
        return rect;
    }

    public static RectF rectToRectF(Rect rect) {
        return new RectF((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom);
    }

    public static void prepareMatrix(Matrix matrix, boolean z, int i, int i2, int i3) {
        matrix.setScale(z ? -1.0f : 1.0f, 1.0f);
        matrix.postRotate((float) i);
        float f = (float) i2;
        float f2 = (float) i3;
        matrix.postScale(f / 2000.0f, f2 / 2000.0f);
        matrix.postTranslate(f / 2.0f, f2 / 2.0f);
    }

    public static void prepareMatrix(Matrix matrix, boolean z, int i, Rect rect) {
        matrix.setScale(z ? -1.0f : 1.0f, 1.0f);
        matrix.postRotate((float) i);
        Matrix matrix2 = new Matrix();
        matrix2.setRectToRect(new RectF(-1000.0f, -1000.0f, 1000.0f, 1000.0f), rectToRectF(rect), ScaleToFit.FILL);
        matrix.setConcat(matrix2, matrix);
    }

    public static String createJpegName(long j, boolean z) {
        String generateName;
        synchronized (sImageFileNamer) {
            generateName = sImageFileNamer.generateName(j, z);
        }
        return generateName;
    }

    public static String createJpegName(long j) {
        String generateName;
        synchronized (sImageFileNamer) {
            generateName = sImageFileNamer.generateName(j, false);
        }
        return generateName;
    }

    public static void broadcastNewPicture(Context context, Uri uri) {
        context.sendBroadcast(new Intent(ACTION_NEW_PICTURE, uri));
        context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
    }

    public static void fadeIn(View view, float f, float f2, long j) {
        if (view.getVisibility() != 0) {
            view.setVisibility(0);
            AlphaAnimation alphaAnimation = new AlphaAnimation(f, f2);
            alphaAnimation.setDuration(j);
            view.startAnimation(alphaAnimation);
        }
    }

    public static void fadeIn(View view) {
        fadeIn(view, 0.0f, 1.0f, 400);
        view.setEnabled(true);
    }

    public static void fadeOut(View view) {
        if (view.getVisibility() == 0) {
            view.setEnabled(false);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(400);
            view.startAnimation(alphaAnimation);
            view.setVisibility(8);
        }
    }

    public static Rect getFinalCropRect(Rect rect, float f) {
        Rect rect2 = new Rect(rect);
        float width = ((float) rect.width()) / ((float) rect.height());
        StringBuilder sb = new StringBuilder();
        sb.append("getFinalCropRect - rect: ");
        sb.append(rect.toString());
        String sb2 = sb.toString();
        String str = TAG;
        Log.d(str, sb2);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("getFinalCropRect - ratios: ");
        sb3.append(width);
        sb3.append(", ");
        sb3.append(f);
        Log.d(str, sb3.toString());
        if (width > f) {
            int height = (int) ((((float) rect.height()) * f) + 0.5f);
            int width2 = ((rect.width() - height) / 2) + rect.left;
            rect2.left = width2;
            rect2.right = width2 + height;
        } else if (width < f) {
            int width3 = (int) ((((float) rect.width()) / f) + 0.5f);
            int height2 = ((rect.height() - width3) / 2) + rect.top;
            rect2.top = height2;
            rect2.bottom = height2 + width3;
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append("getFinalCropRect - final rect: ");
        sb4.append(rect2.toString());
        Log.d(str, sb4.toString());
        return rect2;
    }

    public static int getJpegRotation(int i, int i2) {
        if (i2 == -1) {
            i2 = 0;
        }
        CameraCharacteristics cameraCharacteristics = CameraHolder.instance().getCameraCharacteristics(i);
        if (((Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
            return ((((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() - i2) + 360) % 360;
        }
        return (((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() + i2) % 360;
    }

    public static int getJpegRotationForCamera1(int i, int i2) {
        if (i2 == -1) {
            i2 = 0;
        }
        CameraHolder.CameraInfo cameraInfo = CameraHolder.instance().getCameraInfo()[i];
        if (cameraInfo.facing == 0) {
            return ((cameraInfo.orientation - i2) + 360) % 360;
        }
        return (cameraInfo.orientation + i2) % 360;
    }

    public static Bitmap downSample(byte[] bArr, int i) {
        Options options = new Options();
        options.inSampleSize = i;
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
    }

    public static void setGpsParameters(Parameters parameters, Location location) {
        parameters.removeGpsData();
        parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if ((latitude == 0.0d && longitude == 0.0d) ? false : true) {
                Log.d(TAG, "Set gps location");
                parameters.setGpsLatitude(latitude);
                parameters.setGpsLongitude(longitude);
                parameters.setGpsProcessingMethod(location.getProvider().toUpperCase());
                if (location.hasAltitude()) {
                    parameters.setGpsAltitude(location.getAltitude());
                } else {
                    parameters.setGpsAltitude(0.0d);
                }
                if (location.getTime() != 0) {
                    parameters.setGpsTimestamp(location.getTime() / 1000);
                }
            }
        }
    }

    public static int[] getPhotoPreviewFpsRange(Parameters parameters) {
        return getPhotoPreviewFpsRange(parameters.getSupportedPreviewFpsRange());
    }

    public static int[] getPhotoPreviewFpsRange(List<int[]> list) {
        int size = list.size();
        String str = TAG;
        if (size == 0) {
            Log.e(str, "No suppoted frame rates returned!");
            return null;
        }
        int i = MAX_PREVIEW_FPS_TIMES_1000;
        for (int[] iArr : list) {
            int i2 = iArr[0];
            if (iArr[1] >= PREFERRED_PREVIEW_FPS_TIMES_1000 && i2 <= PREFERRED_PREVIEW_FPS_TIMES_1000 && i2 < i) {
                i = i2;
            }
        }
        int i3 = -1;
        int i4 = 0;
        for (int i5 = 0; i5 < list.size(); i5++) {
            int[] iArr2 = (int[]) list.get(i5);
            int i6 = iArr2[0];
            int i7 = iArr2[1];
            if (i6 == i && i4 < i7) {
                i3 = i5;
                i4 = i7;
            }
        }
        if (i3 >= 0) {
            return (int[]) list.get(i3);
        }
        Log.e(str, "Can't find an appropiate frame rate range!");
        return null;
    }

    public static int[] getMaxPreviewFpsRange(Parameters parameters) {
        List supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        return (supportedPreviewFpsRange == null || supportedPreviewFpsRange.size() <= 0) ? new int[0] : (int[]) supportedPreviewFpsRange.get(supportedPreviewFpsRange.size() - 1);
    }

    public static void playVideo(Activity activity, Uri uri, String str) {
        try {
            if (!((CameraActivity) activity).isSecureCamera()) {
                activity.startActivityForResult(IntentHelper.getVideoPlayerIntent(activity, uri).putExtra("android.intent.extra.TITLE", str).putExtra(KEY_TREAT_UP_AS_BACK, true), 142);
            } else {
                activity.finish();
            }
        } catch (ActivityNotFoundException unused) {
            RotateTextToast.makeText(activity, (CharSequence) activity.getString(C0905R.string.video_err), 0).show();
        }
    }

    public static void showOnMap(Activity activity, double[] dArr) {
        String str = "android.intent.action.VIEW";
        try {
            String format = String.format(Locale.ENGLISH, "http://maps.google.com/maps?f=q&q=(%f,%f)", new Object[]{Double.valueOf(dArr[0]), Double.valueOf(dArr[1])});
            activity.startActivityForResult(new Intent(str, Uri.parse(format)).setComponent(new ComponentName(MAPS_PACKAGE_NAME, MAPS_CLASS_NAME)), 142);
        } catch (ActivityNotFoundException e) {
            String str2 = TAG;
            Log.e(str2, "GMM activity not found!", e);
            try {
                activity.startActivity(new Intent(str, Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f", new Object[]{Double.valueOf(dArr[0]), Double.valueOf(dArr[1])}))));
            } catch (ActivityNotFoundException e2) {
                Log.e(str2, "Map view activity not found!", e2);
                RotateTextToast.makeText(activity, (CharSequence) activity.getString(C0905R.string.map_activity_not_found_err), 0).show();
            }
        }
    }

    public static String dumpStackTrace(int i) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int length = i == 0 ? stackTrace.length : Math.min(i + 3, stackTrace.length);
        String str = new String();
        for (int i2 = 3; i2 < length; i2++) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("\t");
            sb.append(stackTrace[i2].toString());
            sb.append(10);
            str = sb.toString();
        }
        return str;
    }

    public static boolean volumeKeyShutterDisable(Context context) {
        return context.getResources().getBoolean(C0905R.bool.volume_key_shutter_disable);
    }

    public static int determineRatio(int i, int i2) {
        if (i2 != 0) {
            return determineRatio(((float) i) / ((float) i2));
        }
        return 0;
    }

    public static String millisecondToTimeString(long j, boolean z) {
        long j2 = j / 1000;
        long j3 = j2 / 60;
        long j4 = j3 / 60;
        long j5 = j3 - (j4 * 60);
        long j6 = j2 - (j3 * 60);
        StringBuilder sb = new StringBuilder();
        if (j4 > 0) {
            if (j4 < 10) {
                sb.append('0');
            }
            sb.append(j4);
            sb.append(':');
        }
        if (j5 < 10) {
            sb.append('0');
        }
        sb.append(j5);
        sb.append(':');
        if (j6 < 10) {
            sb.append('0');
        }
        sb.append(j6);
        if (z) {
            sb.append('.');
            long j7 = (j - (j2 * 1000)) / 10;
            if (j7 < 10) {
                sb.append('0');
            }
            sb.append(j7);
        }
        return sb.toString();
    }

    public static int dip2px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static void saveDialogShowConfig(Context context, String str, boolean z) {
        Editor edit = context.getSharedPreferences(DIALOG_CONFIG, 0).edit();
        edit.putBoolean(str, z);
        edit.apply();
    }

    public static boolean loadDialogShowConfig(Context context, String str) {
        return context.getSharedPreferences(DIALOG_CONFIG, 0).getBoolean(str, true);
    }

    public static Bitmap adjustPhotoRotation(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.setRotate((float) i, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
