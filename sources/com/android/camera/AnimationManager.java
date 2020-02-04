package com.android.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class AnimationManager {
    public static final float FLASH_ALPHA_END = 0.0f;
    public static final float FLASH_ALPHA_START = 0.3f;
    public static final int FLASH_DURATION = 300;
    public static final int HOLD_DURATION = 2500;
    public static final int SHRINK_DURATION = 400;
    public static final int SLIDE_DURATION = 1100;
    /* access modifiers changed from: private */
    public AnimatorSet mCaptureAnimator;
    /* access modifiers changed from: private */
    public ObjectAnimator mFlashAnim;

    public void startCaptureAnimation(View view) {
        final View view2 = view;
        AnimatorSet animatorSet = this.mCaptureAnimator;
        if (animatorSet != null && animatorSet.isStarted()) {
            this.mCaptureAnimator.cancel();
        }
        View view3 = (View) view.getParent();
        float width = (float) (view3.getWidth() - view.getLeft());
        float width2 = ((float) view3.getWidth()) / ((float) view.getWidth());
        float height = ((float) view3.getHeight()) / ((float) view.getHeight());
        if (width2 <= height) {
            width2 = height;
        }
        int left = view.getLeft() + (view.getWidth() / 2);
        int top = view.getTop() + (view.getHeight() / 2);
        float[] fArr = {0.0f, width};
        String str = "translationX";
        ObjectAnimator duration = ObjectAnimator.ofFloat(view2, str, fArr).setDuration(1100);
        duration.setStartDelay(2900);
        ObjectAnimator duration2 = ObjectAnimator.ofFloat(view2, "translationY", new float[]{(float) ((view3.getHeight() / 2) - top), 0.0f}).setDuration(400);
        duration2.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                view2.setClickable(true);
            }
        });
        this.mCaptureAnimator = new AnimatorSet();
        this.mCaptureAnimator.playTogether(new Animator[]{ObjectAnimator.ofFloat(view2, "scaleX", new float[]{width2, 1.0f}).setDuration(400), ObjectAnimator.ofFloat(view2, "scaleY", new float[]{width2, 1.0f}).setDuration(400), ObjectAnimator.ofFloat(view2, str, new float[]{(float) ((view3.getWidth() / 2) - left), 0.0f}).setDuration(400), duration2, duration});
        this.mCaptureAnimator.addListener(new AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                view2.setClickable(false);
                view2.setVisibility(0);
            }

            public void onAnimationEnd(Animator animator) {
                view2.setScaleX(1.0f);
                view2.setScaleX(1.0f);
                view2.setTranslationX(0.0f);
                view2.setTranslationY(0.0f);
                view2.setVisibility(4);
                AnimationManager.this.mCaptureAnimator.removeAllListeners();
                AnimationManager.this.mCaptureAnimator = null;
            }

            public void onAnimationCancel(Animator animator) {
                view2.setVisibility(4);
            }
        });
        this.mCaptureAnimator.start();
    }

    public void startFlashAnimation(final View view) {
        ObjectAnimator objectAnimator = this.mFlashAnim;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            this.mFlashAnim.cancel();
        }
        this.mFlashAnim = ObjectAnimator.ofFloat(view, "alpha", new float[]{0.3f, 0.0f});
        this.mFlashAnim.setDuration(300);
        this.mFlashAnim.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                view.setVisibility(0);
            }

            public void onAnimationEnd(Animator animator) {
                view.setAlpha(0.0f);
                view.setVisibility(8);
                AnimationManager.this.mFlashAnim.removeAllListeners();
                AnimationManager.this.mFlashAnim = null;
            }
        });
        this.mFlashAnim.start();
    }

    public void cancelAnimations() {
        ObjectAnimator objectAnimator = this.mFlashAnim;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            this.mFlashAnim.cancel();
        }
        AnimatorSet animatorSet = this.mCaptureAnimator;
        if (animatorSet != null && animatorSet.isStarted()) {
            this.mCaptureAnimator.cancel();
        }
    }
}
