package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.camera.SettingsManager;
import java.util.ArrayList;
import java.util.Iterator;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ProMode */
public class ProMode extends View {
    private static final int BLUE = -12151813;
    private static final int DOT_SIZE = 10;
    private static final int DRAG_X_THRESHOLD = 30;
    private static final int DRAG_Y_THRESHOLD = 100;
    public static final int EXPOSURE_MODE = 0;
    public static final int ISO_MODE = 3;
    public static final int MANUAL_MODE = 1;
    public static final int NO_MODE = -1;
    private static final int SELECTED_DOT_SIZE = 20;
    private static final int WB_ICON_SIZE = 80;
    public static final int WHITE_BALANCE_MODE = 2;
    private static final int[] wbIcons = {C0905R.C0906drawable.auto, C0905R.C0906drawable.incandecent, C0905R.C0906drawable.fluorescent, C0905R.C0906drawable.sunlight, C0905R.C0906drawable.cloudy};
    private static final int[] wbIconsBlue = {C0905R.C0906drawable.auto_blue, C0905R.C0906drawable.incandecent_blue, C0905R.C0906drawable.fluorescent_blue, C0905R.C0906drawable.sunlight_blue, C0905R.C0906drawable.cloudy_blue};
    private float[] curveCoordinate = new float[2];
    private ArrayList<View> mAddedViews;
    private float mClickThreshold;
    private Context mContext;
    private int mCurveHeight;
    private int mCurveLeft;
    private PathMeasure mCurveMeasure;
    private Path mCurvePath = new Path();
    private int mCurveRight;
    private int mCurveY;
    private int mHeight;
    private int mIndex;
    private int mMode = -1;
    private int mNums;
    private int mOrientation;
    private Paint mPaint = new Paint();
    private ViewGroup mParent;
    private Point[] mPoints;
    private SettingsManager mSettingsManager;
    private float mSlider = -1.0f;
    private int mStride;
    private OneUICameraControls mUI;
    private int mWidth;
    private float minFocus = -1.0f;

    private String getKey(int i) {
        if (i == 0) {
            return "pref_camera_exposure_key";
        }
        if (i == 2) {
            return "pref_camera_whitebalance_key";
        }
        if (i != 3) {
            return null;
        }
        return SettingsManager.KEY_ISO;
    }

    public ProMode(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mPaint.setStrokeWidth(7.0f);
        this.mSettingsManager = SettingsManager.getInstance();
    }

    private void init() {
        init(0);
        init(2);
        init(3);
        this.mUI.updateProModeText(1, this.mContext.getResources().getString(C0905R.string.manual));
    }

    private void init(int i) {
        String key = getKey(i);
        if (key != null) {
            int valueIndex = this.mSettingsManager.getValueIndex(key);
            this.mUI.updateProModeText(i, this.mSettingsManager.getEntries(key)[valueIndex].toString());
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mMode != -1) {
            this.mPaint.setColor(-1);
            this.mPaint.setStyle(Style.STROKE);
            canvas.drawPath(this.mCurvePath, this.mPaint);
        }
        this.mPaint.setStyle(Style.FILL);
        if (this.mMode == 1) {
            this.mPaint.setColor(-1);
            canvas.drawCircle((float) this.mCurveLeft, (float) this.mCurveY, 10.0f, this.mPaint);
            canvas.drawCircle((float) this.mCurveRight, (float) this.mCurveY, 10.0f, this.mPaint);
            this.mPaint.setColor(BLUE);
            if (this.mSlider >= 0.0f) {
                PathMeasure pathMeasure = this.mCurveMeasure;
                pathMeasure.getPosTan(pathMeasure.getLength() * this.mSlider, this.curveCoordinate, null);
                float[] fArr = this.curveCoordinate;
                canvas.drawCircle(fArr[0], fArr[1], 20.0f, this.mPaint);
                return;
            }
            return;
        }
        for (int i = 0; i < this.mNums; i++) {
            if (i == this.mIndex) {
                this.mPaint.setColor(BLUE);
                Point[] pointArr = this.mPoints;
                canvas.drawCircle((float) pointArr[i].x, (float) pointArr[i].y, 20.0f, this.mPaint);
            } else {
                this.mPaint.setColor(-1);
                Point[] pointArr2 = this.mPoints;
                canvas.drawCircle((float) pointArr2[i].x, (float) pointArr2[i].y, 10.0f, this.mPaint);
            }
        }
    }

    public void initialize(OneUICameraControls oneUICameraControls) {
        this.mParent = (ViewGroup) getParent();
        this.mUI = oneUICameraControls;
        init();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mWidth = i3 - i;
        this.mHeight = i4 - i2;
        int i5 = this.mWidth;
        this.mCurveLeft = i5 / 10;
        int i6 = this.mCurveLeft;
        this.mCurveRight = i5 - i6;
        this.mCurveHeight = i5 / 7;
        this.mCurveY = (int) (((double) this.mHeight) * 0.67d);
        float f = (float) ((i6 + this.mCurveRight) / 2);
        this.mCurvePath.reset();
        this.mCurvePath.moveTo((float) this.mCurveLeft, (float) this.mCurveY);
        Path path = this.mCurvePath;
        int i7 = this.mCurveY;
        path.quadTo(f, (float) (i7 - this.mCurveHeight), (float) this.mCurveRight, (float) i7);
        this.mCurveMeasure = new PathMeasure(this.mCurvePath, false);
    }

    public void reinit() {
        init();
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
        if (this.mAddedViews != null) {
            int i2 = this.mOrientation;
            if (i2 == 90 || i2 == 270) {
                i2 += 180;
            }
            int i3 = i2 % 360;
            Iterator it = this.mAddedViews.iterator();
            while (it.hasNext()) {
                ((View) it.next()).setRotation((float) i3);
            }
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int i) {
        View view;
        int i2 = this.mMode;
        this.mMode = i;
        String str = SettingsManager.KEY_FOCUS_DISTANCE;
        if (i2 == 1 && i != 1) {
            this.mSettingsManager.setFocusDistance(str, true, -1.0f, -1.0f);
        }
        removeViews();
        if (this.mMode == -1) {
            setVisibility(4);
            return;
        }
        setVisibility(0);
        this.mIndex = -1;
        String currentKey = currentKey();
        if (this.mMode == 1) {
            SettingsManager settingsManager = this.mSettingsManager;
            this.minFocus = settingsManager.getMinimumFocusDistance(settingsManager.getCurrentCameraId());
            setSlider(this.mSettingsManager.getFocusValue(str), true);
            int i3 = this.mCurveRight - this.mCurveLeft;
            for (int i4 = 0; i4 < 2; i4++) {
                TextView textView = new TextView(this.mContext);
                String string = this.mContext.getResources().getString(C0905R.string.pref_camera_focusmode_entry_infinity);
                if (i4 == 1) {
                    string = this.mContext.getResources().getString(C0905R.string.pref_camera_focusmode_entry_macro);
                }
                textView.setText(string);
                textView.setTextColor(-1);
                textView.measure(0, 0);
                textView.setLayoutParams(new LayoutParams(textView.getMeasuredWidth(), textView.getMeasuredHeight()));
                textView.setX((float) ((this.mCurveLeft + (i4 * i3)) - (textView.getMeasuredWidth() / 2)));
                textView.setY((float) (this.mCurveY - (textView.getMeasuredHeight() * 2)));
                this.mParent.addView(textView);
                this.mAddedViews.add(textView);
            }
        } else if (currentKey != null) {
            CharSequence[] entries = this.mSettingsManager.getEntries(currentKey);
            int length = this.mSettingsManager.getEntryValues(currentKey).length;
            int valueIndex = this.mSettingsManager.getValueIndex(currentKey);
            updateSlider(length);
            for (int i5 = 0; i5 < length; i5++) {
                if (this.mMode == 2) {
                    ImageView imageView = new ImageView(this.mContext);
                    imageView.setImageResource(wbIcons[i5]);
                    imageView.setLayoutParams(new LayoutParams(WB_ICON_SIZE, WB_ICON_SIZE));
                    imageView.setX((float) (this.mPoints[i5].x - 40));
                    imageView.setY((float) (this.mPoints[i5].y - 160));
                    view = imageView;
                } else {
                    TextView textView2 = new TextView(this.mContext);
                    textView2.setText(entries[i5]);
                    textView2.setTextColor(-1);
                    textView2.measure(0, 0);
                    textView2.setLayoutParams(new LayoutParams(textView2.getMeasuredWidth(), textView2.getMeasuredHeight()));
                    textView2.setX((float) (this.mPoints[i5].x - (textView2.getMeasuredWidth() / 2)));
                    textView2.setY((float) (this.mPoints[i5].y - (textView2.getMeasuredHeight() * 2)));
                    view = textView2;
                }
                this.mParent.addView(view);
                this.mAddedViews.add(view);
            }
            setIndex(valueIndex, true);
        } else {
            return;
        }
        setOrientation(this.mOrientation);
    }

    private String currentKey() {
        return getKey(this.mMode);
    }

    private void updateSlider(int i) {
        this.mNums = i;
        int i2 = this.mCurveRight - this.mCurveLeft;
        int i3 = this.mNums;
        this.mStride = i2 / (i3 - 1);
        this.mClickThreshold = ((float) this.mStride) * 0.45f;
        this.mPoints = new Point[i3];
        float f = 1.0f / ((float) (i3 - 1));
        for (int i4 = 0; i4 < this.mNums; i4++) {
            PathMeasure pathMeasure = this.mCurveMeasure;
            pathMeasure.getPosTan(pathMeasure.getLength() * ((float) i4) * f, this.curveCoordinate, null);
            Point[] pointArr = this.mPoints;
            float[] fArr = this.curveCoordinate;
            pointArr[i4] = new Point((int) fArr[0], (int) fArr[1]);
        }
        invalidate();
    }

    public void setSlider(float f, boolean z) {
        this.mSlider = f;
        this.mSettingsManager.setFocusDistance(SettingsManager.KEY_FOCUS_DISTANCE, z, this.mSlider, this.minFocus);
        this.mUI.updateProModeText(this.mMode, this.mContext.getResources().getString(C0905R.string.manual));
        invalidate();
    }

    private void setIndex(int i, boolean z) {
        if (this.mIndex != i || z) {
            int i2 = this.mIndex;
            if (i2 != -1) {
                View view = (View) this.mAddedViews.get(i2);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(-1);
                } else if ((view instanceof ImageView) && this.mMode == 2) {
                    ((ImageView) view).setImageResource(wbIcons[this.mIndex]);
                }
            }
            this.mIndex = i;
            String currentKey = currentKey();
            View view2 = (View) this.mAddedViews.get(this.mIndex);
            if (view2 instanceof TextView) {
                ((TextView) view2).setTextColor(BLUE);
            } else if ((view2 instanceof ImageView) && this.mMode == 2) {
                ((ImageView) view2).setImageResource(wbIconsBlue[this.mIndex]);
            }
            if (currentKey != null) {
                this.mSettingsManager.setValueIndex(currentKey, this.mIndex);
            }
            this.mUI.updateProModeText(this.mMode, this.mSettingsManager.getEntries(currentKey)[this.mIndex].toString());
            invalidate();
        }
    }

    private void removeViews() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (this.mAddedViews != null) {
            for (int i = 0; i < this.mAddedViews.size(); i++) {
                viewGroup.removeView((View) this.mAddedViews.get(i));
            }
        }
        this.mAddedViews = new ArrayList<>();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mMode == 1) {
            float slider = getSlider(motionEvent.getX(), motionEvent.getY());
            if (slider >= 0.0f) {
                setSlider(slider, false);
            }
            return true;
        }
        int findButton = findButton(motionEvent.getX(), motionEvent.getY());
        if (findButton != -1) {
            setIndex(findButton, false);
        }
        return true;
    }

    private int findButton(float f, float f2) {
        for (int i = 0; i < this.mNums; i++) {
            float abs = Math.abs(((float) this.mPoints[i].x) - f);
            float abs2 = Math.abs(((float) this.mPoints[i].y) - f2);
            float f3 = (abs * abs) + (abs2 * abs2);
            float f4 = this.mClickThreshold;
            if (f3 < f4 * f4) {
                return i;
            }
        }
        return -1;
    }

    private float getSlider(float f, float f2) {
        int i = this.mCurveLeft;
        if (f > ((float) (i - 30)) && f < ((float) (this.mCurveRight + 30))) {
            int i2 = this.mCurveY;
            if (f2 > ((float) ((i2 - this.mCurveHeight) - 100)) && f2 < ((float) (i2 + 100))) {
                if (f < ((float) i)) {
                    f = (float) i;
                }
                int i3 = this.mCurveRight;
                if (f > ((float) i3)) {
                    f = (float) i3;
                }
                int i4 = this.mCurveLeft;
                return (f - ((float) i4)) / ((float) (this.mCurveRight - i4));
            }
        }
        return -1.0f;
    }

    public void resetEVandWB() {
        if (this.mSettingsManager != null) {
            String string = getResources().getString(C0905R.string.pref_exposure_default);
            String string2 = getResources().getString(C0905R.string.pref_camera2_whitebalance_default);
            this.mSettingsManager.setValue("pref_camera_exposure_key", string);
            this.mSettingsManager.setValue("pref_camera_whitebalance_key", string2);
        }
    }
}
