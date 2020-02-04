package android.support.p000v4.media.subtitle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.media.SubtitleData2;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionEvent;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionPenAttr;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionPenColor;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionPenLocation;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionWindow;
import android.support.p000v4.media.subtitle.Cea708CCParser.CaptionWindowAttr;
import android.support.p000v4.media.subtitle.SubtitleController.Renderer;
import android.support.p000v4.media.subtitle.SubtitleTrack.RenderingWidget;
import android.support.p000v4.media.subtitle.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.asus.scenedetectlib.BuildConfig;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@RequiresApi(28)
@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer */
public class Cea708CaptionRenderer extends Renderer {
    private Cea708CCWidget mCCWidget;
    private final Context mContext;

    /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget */
    class Cea708CCWidget extends ClosedCaptionWidget implements DisplayListener {
        private final CCHandler mCCHandler;

        /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCHandler */
        class CCHandler implements Callback {
            private static final int CAPTION_ALL_WINDOWS_BITMAP = 255;
            private static final long CAPTION_CLEAR_INTERVAL_MS = 60000;
            private static final int CAPTION_WINDOWS_MAX = 8;
            private static final boolean DEBUG = false;
            private static final int MSG_CAPTION_CLEAR = 2;
            private static final int MSG_DELAY_CANCEL = 1;
            private static final String TAG = "CCHandler";
            private static final int TENTHS_OF_SECOND_IN_MILLIS = 100;
            private final CCLayout mCCLayout;
            private final CCWindowLayout[] mCaptionWindowLayouts = new CCWindowLayout[8];
            private CCWindowLayout mCurrentWindowLayout;
            private final Handler mHandler;
            private boolean mIsDelayed = false;
            private final ArrayList<CaptionEvent> mPendingCaptionEvents = new ArrayList<>();

            CCHandler(CCLayout cCLayout) {
                this.mCCLayout = cCLayout;
                this.mHandler = new Handler(this);
            }

            public boolean handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    delayCancel();
                    return true;
                } else if (i != 2) {
                    return false;
                } else {
                    clearWindows(255);
                    return true;
                }
            }

            public void processCaptionEvent(CaptionEvent captionEvent) {
                if (this.mIsDelayed) {
                    this.mPendingCaptionEvents.add(captionEvent);
                    return;
                }
                switch (captionEvent.type) {
                    case 1:
                        sendBufferToCurrentWindow((String) captionEvent.obj);
                        break;
                    case 2:
                        sendControlToCurrentWindow(((Character) captionEvent.obj).charValue());
                        break;
                    case 3:
                        setCurrentWindowLayout(((Integer) captionEvent.obj).intValue());
                        break;
                    case 4:
                        clearWindows(((Integer) captionEvent.obj).intValue());
                        break;
                    case 5:
                        displayWindows(((Integer) captionEvent.obj).intValue());
                        break;
                    case 6:
                        hideWindows(((Integer) captionEvent.obj).intValue());
                        break;
                    case 7:
                        toggleWindows(((Integer) captionEvent.obj).intValue());
                        break;
                    case 8:
                        deleteWindows(((Integer) captionEvent.obj).intValue());
                        break;
                    case 9:
                        delay(((Integer) captionEvent.obj).intValue());
                        break;
                    case 10:
                        delayCancel();
                        break;
                    case 11:
                        reset();
                        break;
                    case 12:
                        setPenAttr((CaptionPenAttr) captionEvent.obj);
                        break;
                    case 13:
                        setPenColor((CaptionPenColor) captionEvent.obj);
                        break;
                    case 14:
                        setPenLocation((CaptionPenLocation) captionEvent.obj);
                        break;
                    case 15:
                        setWindowAttr((CaptionWindowAttr) captionEvent.obj);
                        break;
                    case 16:
                        defineWindow((CaptionWindow) captionEvent.obj);
                        break;
                }
            }

            private void setCurrentWindowLayout(int i) {
                if (i >= 0) {
                    CCWindowLayout[] cCWindowLayoutArr = this.mCaptionWindowLayouts;
                    if (i < cCWindowLayoutArr.length) {
                        CCWindowLayout cCWindowLayout = cCWindowLayoutArr[i];
                        if (cCWindowLayout != null) {
                            this.mCurrentWindowLayout = cCWindowLayout;
                        }
                    }
                }
            }

            private ArrayList<CCWindowLayout> getWindowsFromBitmap(int i) {
                ArrayList<CCWindowLayout> arrayList = new ArrayList<>();
                for (int i2 = 0; i2 < 8; i2++) {
                    if (((1 << i2) & i) != 0) {
                        CCWindowLayout cCWindowLayout = this.mCaptionWindowLayouts[i2];
                        if (cCWindowLayout != null) {
                            arrayList.add(cCWindowLayout);
                        }
                    }
                }
                return arrayList;
            }

            private void clearWindows(int i) {
                if (i != 0) {
                    Iterator it = getWindowsFromBitmap(i).iterator();
                    while (it.hasNext()) {
                        ((CCWindowLayout) it.next()).clear();
                    }
                }
            }

            private void displayWindows(int i) {
                if (i != 0) {
                    Iterator it = getWindowsFromBitmap(i).iterator();
                    while (it.hasNext()) {
                        ((CCWindowLayout) it.next()).show();
                    }
                }
            }

            private void hideWindows(int i) {
                if (i != 0) {
                    Iterator it = getWindowsFromBitmap(i).iterator();
                    while (it.hasNext()) {
                        ((CCWindowLayout) it.next()).hide();
                    }
                }
            }

            private void toggleWindows(int i) {
                if (i != 0) {
                    Iterator it = getWindowsFromBitmap(i).iterator();
                    while (it.hasNext()) {
                        CCWindowLayout cCWindowLayout = (CCWindowLayout) it.next();
                        if (cCWindowLayout.isShown()) {
                            cCWindowLayout.hide();
                        } else {
                            cCWindowLayout.show();
                        }
                    }
                }
            }

            private void deleteWindows(int i) {
                if (i != 0) {
                    Iterator it = getWindowsFromBitmap(i).iterator();
                    while (it.hasNext()) {
                        CCWindowLayout cCWindowLayout = (CCWindowLayout) it.next();
                        cCWindowLayout.removeFromCaptionView();
                        this.mCaptionWindowLayouts[cCWindowLayout.getCaptionWindowId()] = null;
                    }
                }
            }

            public void reset() {
                this.mCurrentWindowLayout = null;
                this.mIsDelayed = false;
                this.mPendingCaptionEvents.clear();
                for (int i = 0; i < 8; i++) {
                    CCWindowLayout[] cCWindowLayoutArr = this.mCaptionWindowLayouts;
                    if (cCWindowLayoutArr[i] != null) {
                        cCWindowLayoutArr[i].removeFromCaptionView();
                    }
                    this.mCaptionWindowLayouts[i] = null;
                }
                this.mCCLayout.setVisibility(4);
                this.mHandler.removeMessages(2);
            }

            private void setWindowAttr(CaptionWindowAttr captionWindowAttr) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.setWindowAttr(captionWindowAttr);
                }
            }

            private void defineWindow(CaptionWindow captionWindow) {
                if (captionWindow != null) {
                    int i = captionWindow.f55id;
                    if (i >= 0) {
                        CCWindowLayout[] cCWindowLayoutArr = this.mCaptionWindowLayouts;
                        if (i < cCWindowLayoutArr.length) {
                            CCWindowLayout cCWindowLayout = cCWindowLayoutArr[i];
                            if (cCWindowLayout == null) {
                                cCWindowLayout = new CCWindowLayout(Cea708CCWidget.this, this.mCCLayout.getContext());
                            }
                            cCWindowLayout.initWindow(this.mCCLayout, captionWindow);
                            this.mCaptionWindowLayouts[i] = cCWindowLayout;
                            this.mCurrentWindowLayout = cCWindowLayout;
                        }
                    }
                }
            }

            private void delay(int i) {
                if (i >= 0 && i <= 255) {
                    this.mIsDelayed = true;
                    Handler handler = this.mHandler;
                    handler.sendMessageDelayed(handler.obtainMessage(1), (long) (i * 100));
                }
            }

            private void delayCancel() {
                this.mIsDelayed = false;
                processPendingBuffer();
            }

            private void processPendingBuffer() {
                Iterator it = this.mPendingCaptionEvents.iterator();
                while (it.hasNext()) {
                    processCaptionEvent((CaptionEvent) it.next());
                }
                this.mPendingCaptionEvents.clear();
            }

            private void sendControlToCurrentWindow(char c) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.sendControl(c);
                }
            }

            private void sendBufferToCurrentWindow(String str) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.sendBuffer(str);
                    this.mHandler.removeMessages(2);
                    Handler handler = this.mHandler;
                    handler.sendMessageDelayed(handler.obtainMessage(2), CAPTION_CLEAR_INTERVAL_MS);
                }
            }

            private void setPenAttr(CaptionPenAttr captionPenAttr) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.setPenAttr(captionPenAttr);
                }
            }

            private void setPenColor(CaptionPenColor captionPenColor) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.setPenColor(captionPenColor);
                }
            }

            private void setPenLocation(CaptionPenLocation captionPenLocation) {
                CCWindowLayout cCWindowLayout = this.mCurrentWindowLayout;
                if (cCWindowLayout != null) {
                    cCWindowLayout.setPenLocation(captionPenLocation.row, captionPenLocation.column);
                }
            }
        }

        /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout */
        class CCLayout extends ScaledLayout implements ClosedCaptionLayout {
            private static final float SAFE_TITLE_AREA_SCALE_END_X = 0.9f;
            private static final float SAFE_TITLE_AREA_SCALE_END_Y = 0.9f;
            private static final float SAFE_TITLE_AREA_SCALE_START_X = 0.1f;
            private static final float SAFE_TITLE_AREA_SCALE_START_Y = 0.1f;
            private final ScaledLayout mSafeTitleAreaLayout;

            CCLayout(Context context) {
                super(context);
                this.mSafeTitleAreaLayout = new ScaledLayout(context);
                ScaledLayout scaledLayout = this.mSafeTitleAreaLayout;
                ScaledLayoutParams scaledLayoutParams = new ScaledLayoutParams(0.1f, 0.9f, 0.1f, 0.9f);
                addView(scaledLayout, scaledLayoutParams);
            }

            public void addOrUpdateViewToSafeTitleArea(CCWindowLayout cCWindowLayout, ScaledLayoutParams scaledLayoutParams) {
                if (this.mSafeTitleAreaLayout.indexOfChild(cCWindowLayout) < 0) {
                    this.mSafeTitleAreaLayout.addView(cCWindowLayout, scaledLayoutParams);
                } else {
                    this.mSafeTitleAreaLayout.updateViewLayout(cCWindowLayout, scaledLayoutParams);
                }
            }

            public void removeViewFromSafeTitleArea(CCWindowLayout cCWindowLayout) {
                this.mSafeTitleAreaLayout.removeView(cCWindowLayout);
            }

            public void setCaptionStyle(CaptionStyle captionStyle) {
                int childCount = this.mSafeTitleAreaLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ((CCWindowLayout) this.mSafeTitleAreaLayout.getChildAt(i)).setCaptionStyle(captionStyle);
                }
            }

            public void setFontScale(float f) {
                int childCount = this.mSafeTitleAreaLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ((CCWindowLayout) this.mSafeTitleAreaLayout.getChildAt(i)).setFontScale(f);
                }
            }
        }

        /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCView */
        class CCView extends SubtitleView {
            CCView(Cea708CCWidget cea708CCWidget, Context context) {
                this(cea708CCWidget, context, null);
            }

            CCView(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attributeSet) {
                this(cea708CCWidget, context, attributeSet, 0);
            }

            CCView(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attributeSet, int i) {
                this(context, attributeSet, i, 0);
            }

            CCView(Context context, AttributeSet attributeSet, int i, int i2) {
                super(context, attributeSet, i, i2);
            }

            /* access modifiers changed from: 0000 */
            public void setCaptionStyle(CaptionStyle captionStyle) {
                if (captionStyle.hasForegroundColor()) {
                    setForegroundColor(captionStyle.foregroundColor);
                }
                if (captionStyle.hasBackgroundColor()) {
                    setBackgroundColor(captionStyle.backgroundColor);
                }
                if (captionStyle.hasEdgeType()) {
                    setEdgeType(captionStyle.edgeType);
                }
                if (captionStyle.hasEdgeColor()) {
                    setEdgeColor(captionStyle.edgeColor);
                }
                setTypeface(captionStyle.getTypeface());
            }
        }

        /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCWindowLayout */
        private class CCWindowLayout extends RelativeLayout implements OnLayoutChangeListener {
            private static final int ANCHOR_HORIZONTAL_16_9_MAX = 209;
            private static final int ANCHOR_HORIZONTAL_MODE_CENTER = 1;
            private static final int ANCHOR_HORIZONTAL_MODE_LEFT = 0;
            private static final int ANCHOR_HORIZONTAL_MODE_RIGHT = 2;
            private static final int ANCHOR_MODE_DIVIDER = 3;
            private static final int ANCHOR_RELATIVE_POSITIONING_MAX = 99;
            private static final int ANCHOR_VERTICAL_MAX = 74;
            private static final int ANCHOR_VERTICAL_MODE_BOTTOM = 2;
            private static final int ANCHOR_VERTICAL_MODE_CENTER = 1;
            private static final int ANCHOR_VERTICAL_MODE_TOP = 0;
            private static final int MAX_COLUMN_COUNT_16_9 = 42;
            private static final float PROPORTION_PEN_SIZE_LARGE = 1.25f;
            private static final float PROPORTION_PEN_SIZE_SMALL = 0.75f;
            private static final String TAG = "CCWindowLayout";
            private final SpannableStringBuilder mBuilder;
            private CCLayout mCCLayout;
            private CCView mCCView;
            private CaptionStyle mCaptionStyle;
            private int mCaptionWindowId;
            private final List<CharacterStyle> mCharacterStyles;
            private float mFontScale;
            private int mLastCaptionLayoutHeight;
            private int mLastCaptionLayoutWidth;
            private int mRow;
            private int mRowLimit;
            private float mTextSize;
            private String mWidestChar;

            private int getScreenColumnCount() {
                return 42;
            }

            public void sendControl(char c) {
            }

            public void setPenColor(CaptionPenColor captionPenColor) {
            }

            public void setWindowAttr(CaptionWindowAttr captionWindowAttr) {
            }

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context) {
                this(cea708CCWidget, context, null);
            }

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attributeSet) {
                this(cea708CCWidget, context, attributeSet, 0);
            }

            CCWindowLayout(Cea708CCWidget cea708CCWidget, Context context, AttributeSet attributeSet, int i) {
                this(context, attributeSet, i, 0);
            }

            CCWindowLayout(Context context, AttributeSet attributeSet, int i, int i2) {
                super(context, attributeSet, i, i2);
                this.mRowLimit = 0;
                this.mBuilder = new SpannableStringBuilder();
                this.mCharacterStyles = new ArrayList();
                this.mRow = -1;
                this.mCCView = new CCView(Cea708CCWidget.this, context);
                addView(this.mCCView, new LayoutParams(-2, -2));
                CaptioningManager captioningManager = (CaptioningManager) context.getSystemService("captioning");
                this.mFontScale = captioningManager.getFontScale();
                setCaptionStyle(captioningManager.getUserStyle());
                this.mCCView.setText((CharSequence) BuildConfig.FLAVOR);
                updateWidestChar();
            }

            public void setCaptionStyle(CaptionStyle captionStyle) {
                this.mCaptionStyle = captionStyle;
                this.mCCView.setCaptionStyle(captionStyle);
            }

            public void setFontScale(float f) {
                this.mFontScale = f;
                updateTextSize();
            }

            public int getCaptionWindowId() {
                return this.mCaptionWindowId;
            }

            public void setCaptionWindowId(int i) {
                this.mCaptionWindowId = i;
            }

            public void clear() {
                clearText();
                hide();
            }

            public void show() {
                setVisibility(0);
                requestLayout();
            }

            public void hide() {
                setVisibility(4);
                requestLayout();
            }

            public void setPenAttr(CaptionPenAttr captionPenAttr) {
                this.mCharacterStyles.clear();
                if (captionPenAttr.italic) {
                    this.mCharacterStyles.add(new StyleSpan(2));
                }
                if (captionPenAttr.underline) {
                    this.mCharacterStyles.add(new UnderlineSpan());
                }
                int i = captionPenAttr.penSize;
                if (i == 0) {
                    this.mCharacterStyles.add(new RelativeSizeSpan(PROPORTION_PEN_SIZE_SMALL));
                } else if (i == 2) {
                    this.mCharacterStyles.add(new RelativeSizeSpan(PROPORTION_PEN_SIZE_LARGE));
                }
                int i2 = captionPenAttr.penOffset;
                if (i2 == 0) {
                    this.mCharacterStyles.add(new SubscriptSpan());
                } else if (i2 == 2) {
                    this.mCharacterStyles.add(new SuperscriptSpan());
                }
            }

            public void setPenLocation(int i, int i2) {
                int i3 = this.mRow;
                if (i3 >= 0) {
                    while (i3 < i) {
                        appendText("\n");
                        i3++;
                    }
                }
                this.mRow = i;
            }

            public void sendBuffer(String str) {
                appendText(str);
            }

            /* JADX WARNING: Removed duplicated region for block: B:44:0x0122  */
            /* JADX WARNING: Removed duplicated region for block: B:50:0x013a  */
            /* JADX WARNING: Removed duplicated region for block: B:53:0x015e  */
            /* JADX WARNING: Removed duplicated region for block: B:54:0x0162  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void initWindow(android.support.p000v4.media.subtitle.Cea708CaptionRenderer.Cea708CCWidget.CCLayout r19, android.support.p000v4.media.subtitle.Cea708CCParser.CaptionWindow r20) {
                /*
                    r18 = this;
                    r0 = r18
                    r1 = r19
                    r2 = r20
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout r3 = r0.mCCLayout
                    if (r3 == r1) goto L_0x0019
                    if (r3 == 0) goto L_0x000f
                    r3.removeOnLayoutChangeListener(r0)
                L_0x000f:
                    r0.mCCLayout = r1
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout r1 = r0.mCCLayout
                    r1.addOnLayoutChangeListener(r0)
                    r18.updateWidestChar()
                L_0x0019:
                    int r1 = r2.anchorVertical
                    float r1 = (float) r1
                    boolean r3 = r2.relativePositioning
                    r4 = 99
                    if (r3 == 0) goto L_0x0024
                    r3 = r4
                    goto L_0x0026
                L_0x0024:
                    r3 = 74
                L_0x0026:
                    float r3 = (float) r3
                    float r1 = r1 / r3
                    int r3 = r2.anchorHorizontal
                    float r3 = (float) r3
                    boolean r5 = r2.relativePositioning
                    if (r5 == 0) goto L_0x0030
                    goto L_0x0032
                L_0x0030:
                    r4 = 209(0xd1, float:2.93E-43)
                L_0x0032:
                    float r4 = (float) r4
                    float r3 = r3 / r4
                    r4 = 0
                    int r5 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
                    java.lang.String r6 = "CCWindowLayout"
                    r7 = 1065353216(0x3f800000, float:1.0)
                    if (r5 < 0) goto L_0x0041
                    int r5 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1))
                    if (r5 <= 0) goto L_0x005d
                L_0x0041:
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder
                    r5.<init>()
                    java.lang.String r8 = "The vertical position of the anchor point should be at the range of 0 and 1 but "
                    r5.append(r8)
                    r5.append(r1)
                    java.lang.String r5 = r5.toString()
                    android.util.Log.i(r6, r5)
                    float r1 = java.lang.Math.min(r1, r7)
                    float r1 = java.lang.Math.max(r4, r1)
                L_0x005d:
                    int r5 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1))
                    if (r5 < 0) goto L_0x0065
                    int r5 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
                    if (r5 <= 0) goto L_0x0081
                L_0x0065:
                    java.lang.StringBuilder r5 = new java.lang.StringBuilder
                    r5.<init>()
                    java.lang.String r8 = "The horizontal position of the anchor point should be at the range of 0 and 1 but "
                    r5.append(r8)
                    r5.append(r3)
                    java.lang.String r5 = r5.toString()
                    android.util.Log.i(r6, r5)
                    float r3 = java.lang.Math.min(r3, r7)
                    float r3 = java.lang.Math.max(r4, r3)
                L_0x0081:
                    r5 = 17
                    int r6 = r2.anchorId
                    int r8 = r6 % 3
                    r9 = 3
                    int r6 = r6 / r9
                    r10 = 2
                    r11 = 1
                    if (r8 == 0) goto L_0x0114
                    if (r8 == r11) goto L_0x009e
                    if (r8 == r10) goto L_0x0097
                    r16 = r4
                    r17 = r7
                    goto L_0x0120
                L_0x0097:
                    r5 = 5
                    r17 = r3
                    r16 = r4
                    goto L_0x0120
                L_0x009e:
                    float r5 = r7 - r3
                    float r5 = java.lang.Math.min(r5, r3)
                    int r8 = r2.columnCount
                    int r8 = r8 + r11
                    int r12 = r18.getScreenColumnCount()
                    int r8 = java.lang.Math.min(r12, r8)
                    java.lang.StringBuilder r12 = new java.lang.StringBuilder
                    r12.<init>()
                    r13 = 0
                L_0x00b5:
                    if (r13 >= r8) goto L_0x00bf
                    java.lang.String r14 = r0.mWidestChar
                    r12.append(r14)
                    int r13 = r13 + 1
                    goto L_0x00b5
                L_0x00bf:
                    android.graphics.Paint r8 = new android.graphics.Paint
                    r8.<init>()
                    android.view.accessibility.CaptioningManager$CaptionStyle r13 = r0.mCaptionStyle
                    android.graphics.Typeface r13 = r13.getTypeface()
                    r8.setTypeface(r13)
                    float r13 = r0.mTextSize
                    r8.setTextSize(r13)
                    java.lang.String r12 = r12.toString()
                    float r8 = r8.measureText(r12)
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout r12 = r0.mCCLayout
                    int r12 = r12.getWidth()
                    if (r12 <= 0) goto L_0x00f2
                    r12 = 1073741824(0x40000000, float:2.0)
                    float r8 = r8 / r12
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout r12 = r0.mCCLayout
                    int r12 = r12.getWidth()
                    float r12 = (float) r12
                    r13 = 1061997773(0x3f4ccccd, float:0.8)
                    float r12 = r12 * r13
                    float r8 = r8 / r12
                    goto L_0x00f3
                L_0x00f2:
                    r8 = r4
                L_0x00f3:
                    int r12 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1))
                    if (r12 <= 0) goto L_0x0104
                    int r12 = (r8 > r3 ? 1 : (r8 == r3 ? 0 : -1))
                    if (r12 >= 0) goto L_0x0104
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCView r5 = r0.mCCView
                    android.text.Layout$Alignment r12 = android.text.Layout.Alignment.ALIGN_NORMAL
                    r5.setAlignment(r12)
                    float r3 = r3 - r8
                    goto L_0x011b
                L_0x0104:
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCView r8 = r0.mCCView
                    android.text.Layout$Alignment r9 = android.text.Layout.Alignment.ALIGN_CENTER
                    r8.setAlignment(r9)
                    float r8 = r3 - r5
                    float r3 = r3 + r5
                    r17 = r3
                    r16 = r8
                    r5 = r11
                    goto L_0x0120
                L_0x0114:
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCView r5 = r0.mCCView
                    android.text.Layout$Alignment r8 = android.text.Layout.Alignment.ALIGN_NORMAL
                    r5.setAlignment(r8)
                L_0x011b:
                    r16 = r3
                    r17 = r7
                    r5 = r9
                L_0x0120:
                    if (r6 == 0) goto L_0x013a
                    if (r6 == r11) goto L_0x012e
                    if (r6 == r10) goto L_0x0129
                L_0x0126:
                    r14 = r4
                L_0x0127:
                    r15 = r7
                    goto L_0x013e
                L_0x0129:
                    r5 = r5 | 80
                    r15 = r1
                    r14 = r4
                    goto L_0x013e
                L_0x012e:
                    r5 = r5 | 16
                    float r7 = r7 - r1
                    float r3 = java.lang.Math.min(r7, r1)
                    float r4 = r1 - r3
                    float r7 = r1 + r3
                    goto L_0x0126
                L_0x013a:
                    r5 = r5 | 48
                    r14 = r1
                    goto L_0x0127
                L_0x013e:
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout r1 = r0.mCCLayout
                    android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$ScaledLayout$ScaledLayoutParams r3 = new android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$ScaledLayout$ScaledLayoutParams
                    r1.getClass()
                    r12 = r3
                    r13 = r1
                    r12.<init>(r14, r15, r16, r17)
                    r1.addOrUpdateViewToSafeTitleArea(r0, r3)
                    int r1 = r2.f55id
                    r0.setCaptionWindowId(r1)
                    int r1 = r2.rowCount
                    r0.setRowLimit(r1)
                    r0.setGravity(r5)
                    boolean r1 = r2.visible
                    if (r1 == 0) goto L_0x0162
                    r18.show()
                    goto L_0x0165
                L_0x0162:
                    r18.hide()
                L_0x0165:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.media.subtitle.Cea708CaptionRenderer.Cea708CCWidget.CCWindowLayout.initWindow(android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$CCLayout, android.support.v4.media.subtitle.Cea708CCParser$CaptionWindow):void");
            }

            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9 = i3 - i;
                int i10 = i4 - i2;
                if (i9 != this.mLastCaptionLayoutWidth || i10 != this.mLastCaptionLayoutHeight) {
                    this.mLastCaptionLayoutWidth = i9;
                    this.mLastCaptionLayoutHeight = i10;
                    updateTextSize();
                }
            }

            private void updateWidestChar() {
                Paint paint = new Paint();
                paint.setTypeface(this.mCaptionStyle.getTypeface());
                Charset forName = Charset.forName("ISO-8859-1");
                float f = 0.0f;
                for (int i = 0; i < 256; i++) {
                    String str = new String(new byte[]{(byte) i}, forName);
                    float measureText = paint.measureText(str);
                    if (f < measureText) {
                        this.mWidestChar = str;
                        f = measureText;
                    }
                }
                updateTextSize();
            }

            private void updateTextSize() {
                if (this.mCCLayout != null) {
                    StringBuilder sb = new StringBuilder();
                    int screenColumnCount = getScreenColumnCount();
                    for (int i = 0; i < screenColumnCount; i++) {
                        sb.append(this.mWidestChar);
                    }
                    String sb2 = sb.toString();
                    Paint paint = new Paint();
                    paint.setTypeface(this.mCaptionStyle.getTypeface());
                    float f = 0.0f;
                    float f2 = 255.0f;
                    while (f < f2) {
                        float f3 = (f + f2) / 2.0f;
                        paint.setTextSize(f3);
                        if (((float) this.mCCLayout.getWidth()) * 0.8f > paint.measureText(sb2)) {
                            f = f3 + 0.01f;
                        } else {
                            f2 = f3 - 0.01f;
                        }
                    }
                    this.mTextSize = f2 * this.mFontScale;
                    this.mCCView.setTextSize(this.mTextSize);
                }
            }

            public void removeFromCaptionView() {
                CCLayout cCLayout = this.mCCLayout;
                if (cCLayout != null) {
                    cCLayout.removeViewFromSafeTitleArea(this);
                    this.mCCLayout.removeOnLayoutChangeListener(this);
                    this.mCCLayout = null;
                }
            }

            public void setText(String str) {
                updateText(str, false);
            }

            public void appendText(String str) {
                updateText(str, true);
            }

            public void clearText() {
                this.mBuilder.clear();
                this.mCCView.setText((CharSequence) BuildConfig.FLAVOR);
            }

            private void updateText(String str, boolean z) {
                if (!z) {
                    this.mBuilder.clear();
                }
                if (str != null && str.length() > 0) {
                    int length = this.mBuilder.length();
                    this.mBuilder.append(str);
                    for (CharacterStyle characterStyle : this.mCharacterStyles) {
                        SpannableStringBuilder spannableStringBuilder = this.mBuilder;
                        spannableStringBuilder.setSpan(characterStyle, length, spannableStringBuilder.length(), 33);
                    }
                }
                String str2 = "\n";
                String[] split = TextUtils.split(this.mBuilder.toString(), str2);
                String join = TextUtils.join(str2, Arrays.copyOfRange(split, Math.max(0, split.length - (this.mRowLimit + 1)), split.length));
                SpannableStringBuilder spannableStringBuilder2 = this.mBuilder;
                spannableStringBuilder2.delete(0, spannableStringBuilder2.length() - join.length());
                int length2 = this.mBuilder.length() - 1;
                int i = 0;
                while (i <= length2 && this.mBuilder.charAt(i) <= ' ') {
                    i++;
                }
                int i2 = length2;
                while (i2 >= i && this.mBuilder.charAt(i2) <= ' ') {
                    i2--;
                }
                if (i == 0 && i2 == length2) {
                    this.mCCView.setText((CharSequence) this.mBuilder);
                    return;
                }
                SpannableStringBuilder spannableStringBuilder3 = new SpannableStringBuilder();
                spannableStringBuilder3.append(this.mBuilder);
                if (i2 < length2) {
                    spannableStringBuilder3.delete(i2 + 1, length2 + 1);
                }
                if (i > 0) {
                    spannableStringBuilder3.delete(0, i);
                }
                this.mCCView.setText((CharSequence) spannableStringBuilder3);
            }

            public void setRowLimit(int i) {
                if (i >= 0) {
                    this.mRowLimit = i;
                    return;
                }
                throw new IllegalArgumentException("A rowLimit should have a positive number");
            }
        }

        /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$ScaledLayout */
        class ScaledLayout extends ViewGroup {
            private static final boolean DEBUG = false;
            private static final String TAG = "ScaledLayout";
            private Rect[] mRectArray;
            private final Comparator<Rect> mRectTopLeftSorter = new Comparator<Rect>() {
                public int compare(Rect rect, Rect rect2) {
                    int i = rect.top;
                    int i2 = rect2.top;
                    if (i != i2) {
                        return i - i2;
                    }
                    return rect.left - rect2.left;
                }
            };

            /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CCWidget$ScaledLayout$ScaledLayoutParams */
            class ScaledLayoutParams extends ViewGroup.LayoutParams {
                public static final float SCALE_UNSPECIFIED = -1.0f;
                public float scaleEndCol;
                public float scaleEndRow;
                public float scaleStartCol;
                public float scaleStartRow;

                ScaledLayoutParams(float f, float f2, float f3, float f4) {
                    super(-1, -1);
                    this.scaleStartRow = f;
                    this.scaleEndRow = f2;
                    this.scaleStartCol = f3;
                    this.scaleEndCol = f4;
                }

                ScaledLayoutParams(Context context, AttributeSet attributeSet) {
                    super(-1, -1);
                }
            }

            ScaledLayout(Context context) {
                super(context);
            }

            public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
                return new ScaledLayoutParams(getContext(), attributeSet);
            }

            /* access modifiers changed from: protected */
            public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
                return layoutParams instanceof ScaledLayoutParams;
            }

            /* access modifiers changed from: protected */
            public void onMeasure(int i, int i2) {
                int i3;
                int size = MeasureSpec.getSize(i);
                int size2 = MeasureSpec.getSize(i2);
                int paddingLeft = (size - getPaddingLeft()) - getPaddingRight();
                int paddingTop = (size2 - getPaddingTop()) - getPaddingBottom();
                int childCount = getChildCount();
                this.mRectArray = new Rect[childCount];
                int i4 = 0;
                while (i4 < childCount) {
                    View childAt = getChildAt(i4);
                    ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                    if (layoutParams instanceof ScaledLayoutParams) {
                        ScaledLayoutParams scaledLayoutParams = (ScaledLayoutParams) layoutParams;
                        float f = scaledLayoutParams.scaleStartRow;
                        float f2 = scaledLayoutParams.scaleEndRow;
                        float f3 = scaledLayoutParams.scaleStartCol;
                        float f4 = scaledLayoutParams.scaleEndCol;
                        if (f >= 0.0f) {
                            int i5 = (f > 1.0f ? 1 : (f == 1.0f ? 0 : -1));
                            if (i5 <= 0) {
                                if (f2 < f || i5 > 0) {
                                    throw new RuntimeException("A child of ScaledLayout should have a range of scaleEndRow between scaleStartRow and 1");
                                }
                                if (f4 >= 0.0f) {
                                    int i6 = (f4 > 1.0f ? 1 : (f4 == 1.0f ? 0 : -1));
                                    if (i6 <= 0) {
                                        if (f4 < f3 || i6 > 0) {
                                            throw new RuntimeException("A child of ScaledLayout should have a range of scaleEndCol between scaleStartCol and 1");
                                        }
                                        float f5 = (float) paddingLeft;
                                        int i7 = paddingLeft;
                                        float f6 = (float) paddingTop;
                                        int i8 = size;
                                        int i9 = size2;
                                        int i10 = childCount;
                                        this.mRectArray[i4] = new Rect((int) (f3 * f5), (int) (f * f6), (int) (f4 * f5), (int) (f2 * f6));
                                        int makeMeasureSpec = MeasureSpec.makeMeasureSpec((int) (f5 * (f4 - f3)), 1073741824);
                                        childAt.measure(makeMeasureSpec, MeasureSpec.makeMeasureSpec(0, 0));
                                        if (childAt.getMeasuredHeight() > this.mRectArray[i4].height()) {
                                            int measuredHeight = ((childAt.getMeasuredHeight() - this.mRectArray[i4].height()) + 1) / 2;
                                            Rect[] rectArr = this.mRectArray;
                                            Rect rect = rectArr[i4];
                                            rect.bottom += measuredHeight;
                                            Rect rect2 = rectArr[i4];
                                            rect2.top -= measuredHeight;
                                            if (rectArr[i4].top < 0) {
                                                Rect rect3 = rectArr[i4];
                                                rect3.bottom -= rectArr[i4].top;
                                                rectArr[i4].top = 0;
                                            }
                                            Rect[] rectArr2 = this.mRectArray;
                                            if (rectArr2[i4].bottom > paddingTop) {
                                                Rect rect4 = rectArr2[i4];
                                                rect4.top -= rectArr2[i4].bottom - paddingTop;
                                                rectArr2[i4].bottom = paddingTop;
                                            }
                                        }
                                        childAt.measure(makeMeasureSpec, MeasureSpec.makeMeasureSpec((int) (f6 * (f2 - f)), 1073741824));
                                        i4++;
                                        paddingLeft = i7;
                                        size = i8;
                                        size2 = i9;
                                        childCount = i10;
                                    }
                                }
                                throw new RuntimeException("A child of ScaledLayout should have a range of scaleStartCol between 0 and 1");
                            }
                        }
                        throw new RuntimeException("A child of ScaledLayout should have a range of scaleStartRow between 0 and 1");
                    }
                    throw new RuntimeException("A child of ScaledLayout cannot have the UNSPECIFIED scale factors");
                }
                int i11 = size;
                int i12 = size2;
                int i13 = childCount;
                int[] iArr = new int[i13];
                Rect[] rectArr3 = new Rect[i13];
                int i14 = 0;
                for (int i15 = 0; i15 < i13; i15++) {
                    if (getChildAt(i15).getVisibility() == 0) {
                        iArr[i14] = i14;
                        rectArr3[i14] = this.mRectArray[i15];
                        i14++;
                    }
                }
                Arrays.sort(rectArr3, 0, i14, this.mRectTopLeftSorter);
                int i16 = 0;
                while (true) {
                    i3 = i14 - 1;
                    if (i16 >= i3) {
                        break;
                    }
                    int i17 = i16 + 1;
                    for (int i18 = i17; i18 < i14; i18++) {
                        if (Rect.intersects(rectArr3[i16], rectArr3[i18])) {
                            iArr[i18] = iArr[i16];
                            rectArr3[i18].set(rectArr3[i18].left, rectArr3[i16].bottom, rectArr3[i18].right, rectArr3[i16].bottom + rectArr3[i18].height());
                        }
                    }
                    i16 = i17;
                }
                while (i3 >= 0) {
                    if (rectArr3[i3].bottom > paddingTop) {
                        int i19 = rectArr3[i3].bottom - paddingTop;
                        for (int i20 = 0; i20 <= i3; i20++) {
                            if (iArr[i3] == iArr[i20]) {
                                rectArr3[i20].set(rectArr3[i20].left, rectArr3[i20].top - i19, rectArr3[i20].right, rectArr3[i20].bottom - i19);
                            }
                        }
                    }
                    i3--;
                }
                setMeasuredDimension(i11, i12);
            }

            /* access modifiers changed from: protected */
            public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int childCount = getChildCount();
                for (int i5 = 0; i5 < childCount; i5++) {
                    View childAt = getChildAt(i5);
                    if (childAt.getVisibility() != 8) {
                        Rect[] rectArr = this.mRectArray;
                        childAt.layout(rectArr[i5].left + paddingLeft, rectArr[i5].top + paddingTop, rectArr[i5].right + paddingTop, rectArr[i5].bottom + paddingLeft);
                    }
                }
            }

            public void dispatchDraw(Canvas canvas) {
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = getChildAt(i);
                    if (childAt.getVisibility() != 8) {
                        Rect[] rectArr = this.mRectArray;
                        if (i < rectArr.length) {
                            int i2 = rectArr[i].left + paddingLeft;
                            int i3 = rectArr[i].top + paddingTop;
                            int save = canvas.save();
                            canvas.translate((float) i2, (float) i3);
                            childAt.draw(canvas);
                            canvas.restoreToCount(save);
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        Cea708CCWidget(Cea708CaptionRenderer cea708CaptionRenderer, Context context) {
            this(cea708CaptionRenderer, context, null);
        }

        Cea708CCWidget(Cea708CaptionRenderer cea708CaptionRenderer, Context context, AttributeSet attributeSet) {
            this(cea708CaptionRenderer, context, attributeSet, 0);
        }

        Cea708CCWidget(Cea708CaptionRenderer cea708CaptionRenderer, Context context, AttributeSet attributeSet, int i) {
            this(context, attributeSet, i, 0);
        }

        Cea708CCWidget(Context context, AttributeSet attributeSet, int i, int i2) {
            super(context, attributeSet, i, i2);
            this.mCCHandler = new CCHandler((CCLayout) this.mClosedCaptionLayout);
        }

        public ClosedCaptionLayout createCaptionLayout(Context context) {
            return new CCLayout(context);
        }

        public void emitEvent(CaptionEvent captionEvent) {
            this.mCCHandler.processCaptionEvent(captionEvent);
            setSize(getWidth(), getHeight());
            OnChangedListener onChangedListener = this.mListener;
            if (onChangedListener != null) {
                onChangedListener.onChanged(this);
            }
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            ((ViewGroup) this.mClosedCaptionLayout).draw(canvas);
        }
    }

    /* renamed from: android.support.v4.media.subtitle.Cea708CaptionRenderer$Cea708CaptionTrack */
    static class Cea708CaptionTrack extends SubtitleTrack {
        private final Cea708CCParser mCCParser = new Cea708CCParser(this.mRenderingWidget);
        private final Cea708CCWidget mRenderingWidget;

        public void updateView(ArrayList<Cue> arrayList) {
        }

        Cea708CaptionTrack(Cea708CCWidget cea708CCWidget, MediaFormat mediaFormat) {
            super(mediaFormat);
            this.mRenderingWidget = cea708CCWidget;
        }

        public void onData(byte[] bArr, boolean z, long j) {
            this.mCCParser.parse(bArr);
        }

        public RenderingWidget getRenderingWidget() {
            return this.mRenderingWidget;
        }
    }

    public Cea708CaptionRenderer(Context context) {
        this.mContext = context;
    }

    public boolean supports(MediaFormat mediaFormat) {
        String str = "mime";
        if (!mediaFormat.containsKey(str)) {
            return false;
        }
        return SubtitleData2.MIMETYPE_TEXT_CEA_708.equals(mediaFormat.getString(str));
    }

    public SubtitleTrack createTrack(MediaFormat mediaFormat) {
        if (SubtitleData2.MIMETYPE_TEXT_CEA_708.equals(mediaFormat.getString("mime"))) {
            if (this.mCCWidget == null) {
                this.mCCWidget = new Cea708CCWidget(this, this.mContext);
            }
            return new Cea708CaptionTrack(this.mCCWidget, mediaFormat);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("No matching format: ");
        sb.append(mediaFormat.toString());
        throw new RuntimeException(sb.toString());
    }
}
