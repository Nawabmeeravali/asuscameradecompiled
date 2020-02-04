package android.support.p000v4.media.subtitle;

import android.content.Context;
import android.support.annotation.RequiresApi;
import android.support.p000v4.media.subtitle.SubtitleTrack.RenderingWidget;
import android.support.p000v4.media.subtitle.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;

@RequiresApi(28)
/* renamed from: android.support.v4.media.subtitle.ClosedCaptionWidget */
abstract class ClosedCaptionWidget extends ViewGroup implements RenderingWidget {
    protected CaptionStyle mCaptionStyle;
    private final CaptioningChangeListener mCaptioningListener = new CaptioningChangeListener() {
        public void onUserStyleChanged(CaptionStyle captionStyle) {
            ClosedCaptionWidget closedCaptionWidget = ClosedCaptionWidget.this;
            closedCaptionWidget.mCaptionStyle = captionStyle;
            closedCaptionWidget.mClosedCaptionLayout.setCaptionStyle(closedCaptionWidget.mCaptionStyle);
        }

        public void onFontScaleChanged(float f) {
            ClosedCaptionWidget.this.mClosedCaptionLayout.setFontScale(f);
        }
    };
    protected ClosedCaptionLayout mClosedCaptionLayout;
    private boolean mHasChangeListener;
    protected OnChangedListener mListener;
    private final CaptioningManager mManager;

    /* renamed from: android.support.v4.media.subtitle.ClosedCaptionWidget$ClosedCaptionLayout */
    interface ClosedCaptionLayout {
        void setCaptionStyle(CaptionStyle captionStyle);

        void setFontScale(float f);
    }

    public abstract ClosedCaptionLayout createCaptionLayout(Context context);

    ClosedCaptionWidget(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        setLayerType(1, null);
        this.mManager = (CaptioningManager) context.getSystemService("captioning");
        this.mCaptionStyle = this.mManager.getUserStyle();
        this.mClosedCaptionLayout = createCaptionLayout(context);
        this.mClosedCaptionLayout.setCaptionStyle(this.mCaptionStyle);
        this.mClosedCaptionLayout.setFontScale(this.mManager.getFontScale());
        addView((ViewGroup) this.mClosedCaptionLayout, -1, -1);
        requestLayout();
    }

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.mListener = onChangedListener;
    }

    public void setSize(int i, int i2) {
        measure(MeasureSpec.makeMeasureSpec(i, 1073741824), MeasureSpec.makeMeasureSpec(i2, 1073741824));
        layout(0, 0, i, i2);
    }

    public void setVisible(boolean z) {
        if (z) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
        manageChangeListener();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        manageChangeListener();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        manageChangeListener();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        ((ViewGroup) this.mClosedCaptionLayout).measure(i, i2);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        ((ViewGroup) this.mClosedCaptionLayout).layout(i, i2, i3, i4);
    }

    private void manageChangeListener() {
        boolean z = isAttachedToWindow() && getVisibility() == 0;
        if (this.mHasChangeListener != z) {
            this.mHasChangeListener = z;
            if (z) {
                this.mManager.addCaptioningChangeListener(this.mCaptioningListener);
            } else {
                this.mManager.removeCaptioningChangeListener(this.mCaptioningListener);
            }
        }
    }
}
