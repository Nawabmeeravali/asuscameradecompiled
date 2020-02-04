package com.android.camera.p004ui;

import android.content.Context;
import android.content.res.Configuration;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.CountDownView */
public class CountDownView extends FrameLayout {
    private static final int HEIGHT_OFFSET = 210;
    private static final int SET_TIMER_TEXT = 1;
    private static final String TAG = "CAM_CountDownView";
    private static int mBeepOnce;
    private static int mBeepTwice;
    private static SoundPool mSoundPool;
    private Context mContext;
    private Animation mCountDownAnim;
    private TextView mCountDownTitle = null;
    private final Handler mHandler = new MainHandler();
    private OnCountDownFinishedListener mListener;
    private boolean mPlaySound;
    private TextView mRemainingSecondsView;
    /* access modifiers changed from: private */
    public int mRemainingSecs = 0;

    /* renamed from: com.android.camera.ui.CountDownView$MainHandler */
    private class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                CountDownView countDownView = CountDownView.this;
                countDownView.remainingSecondsChanged(countDownView.mRemainingSecs - 1);
            }
        }
    }

    /* renamed from: com.android.camera.ui.CountDownView$OnCountDownFinishedListener */
    public interface OnCountDownFinishedListener {
        void onCountDownFinished();
    }

    public CountDownView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mCountDownAnim = AnimationUtils.loadAnimation(context, C0905R.anim.count_down_exit);
        if (mSoundPool == null) {
            if (context.getResources().getBoolean(C0905R.bool.force_count_down_sound)) {
                mSoundPool = new SoundPool(1, 7, 0);
            } else {
                mSoundPool = new SoundPool(1, 5, 0);
            }
            mBeepOnce = mSoundPool.load(context, C0905R.raw.beep_once, 1);
            mBeepTwice = mSoundPool.load(context, C0905R.raw.beep_twice, 1);
        }
    }

    public void releaseSoundPool() {
        SoundPool soundPool = mSoundPool;
        if (soundPool != null) {
            soundPool.unload(C0905R.raw.beep_once);
            mSoundPool.unload(C0905R.raw.beep_twice);
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public boolean isCountingDown() {
        return this.mRemainingSecs > 0;
    }

    /* access modifiers changed from: private */
    public void remainingSecondsChanged(int i) {
        this.mRemainingSecs = i;
        if (i == 0) {
            setVisibility(4);
            this.mListener.onCountDownFinished();
            return;
        }
        this.mRemainingSecondsView.setText(String.format(getResources().getConfiguration().locale, "%d", new Object[]{Integer.valueOf(i)}));
        this.mCountDownAnim.reset();
        this.mRemainingSecondsView.clearAnimation();
        this.mRemainingSecondsView.startAnimation(this.mCountDownAnim);
        if (mSoundPool == null) {
            if (this.mContext.getResources().getBoolean(C0905R.bool.force_count_down_sound)) {
                mSoundPool = new SoundPool(1, 7, 0);
            } else {
                mSoundPool = new SoundPool(1, 5, 0);
            }
            mBeepOnce = mSoundPool.load(this.mContext, C0905R.raw.beep_once, 1);
            mBeepTwice = mSoundPool.load(this.mContext, C0905R.raw.beep_twice, 1);
        }
        if (this.mPlaySound) {
            SoundPool soundPool = mSoundPool;
            if (soundPool != null) {
                if (i == 1) {
                    soundPool.play(mBeepTwice, 1.0f, 1.0f, 0, 0, 1.0f);
                } else if (i <= 3) {
                    soundPool.play(mBeepOnce, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
        }
        this.mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mRemainingSecondsView = (TextView) findViewById(C0905R.C0907id.remaining_seconds);
        this.mCountDownTitle = (TextView) findViewById(C0905R.C0907id.count_down_title);
    }

    public void onConfigurationChanged(Configuration configuration) {
        TextView textView = this.mCountDownTitle;
        if (textView != null) {
            LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(C0905R.dimen.count_down_title_margin_top);
            this.mCountDownTitle.setLayoutParams(layoutParams);
        }
    }

    public void setCountDownFinishedListener(OnCountDownFinishedListener onCountDownFinishedListener) {
        this.mListener = onCountDownFinishedListener;
    }

    public void startCountDown(int i, boolean z) {
        if (i <= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid input for countdown timer: ");
            sb.append(i);
            sb.append(" seconds");
            Log.w(TAG, sb.toString());
            return;
        }
        setVisibility(0);
        this.mPlaySound = z;
        remainingSecondsChanged(i);
    }

    public void cancelCountDown() {
        if (this.mRemainingSecs > 0) {
            this.mRemainingSecs = 0;
            this.mHandler.removeMessages(1);
            setVisibility(4);
        }
    }

    public void setOrientation(int i) {
        int i2;
        float f = (float) (-i);
        this.mRemainingSecondsView.setRotation(f);
        this.mCountDownTitle.setRotation(f);
        int i3 = getResources().getDisplayMetrics().widthPixels;
        int measuredHeight = this.mCountDownTitle.getMeasuredHeight();
        int i4 = 0;
        if (measuredHeight == 0) {
            measure(0, 0);
            measuredHeight = this.mCountDownTitle.getMeasuredHeight();
        }
        if (i == 90) {
            int i5 = (i3 - measuredHeight) / 2;
            i4 = -i5;
            i2 = i5 + HEIGHT_OFFSET;
        } else if (i == 180 || i != 270) {
            i2 = 0;
        } else {
            i4 = (i3 - measuredHeight) / 2;
            i2 = i4 + HEIGHT_OFFSET;
        }
        this.mCountDownTitle.setTranslationX((float) i4);
        this.mCountDownTitle.setTranslationY((float) i2);
    }
}
