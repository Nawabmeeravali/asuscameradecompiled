package com.android.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.camera.p004ui.DotsView;
import com.android.camera.p004ui.DotsViewItem;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.util.CameraUtil;
import org.codeaurora.snapcam.C0905R;

public class SceneModeActivity extends Activity {
    private MyPagerAdapter mAdapter;
    private RotateImageView mButton;
    private View mCloseButton;
    private int mCurrentScene;
    /* access modifiers changed from: private */
    public DotsView mDotsView;
    private int mElemPerPage = 16;
    private CharSequence[] mEntries;
    private int mNumElement;
    private int mNumPage;
    private ViewPager mPager;
    private SettingsManager mSettingsManager;
    private int[] mThumbnails;

    private static class PageItems implements DotsViewItem {
        int number;

        public boolean isChosen(int i) {
            return true;
        }

        public PageItems(int i) {
            this.number = i;
        }

        public int getTotalItemNums() {
            return this.number;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        final boolean booleanExtra = getIntent().getBooleanExtra(CameraUtil.KEY_IS_SECURE_CAMERA, false);
        if (booleanExtra) {
            setShowInLockScreen();
        }
        setContentView(C0905R.layout.scene_mode_menu_layout);
        this.mSettingsManager = SettingsManager.getInstance();
        SettingsManager settingsManager = this.mSettingsManager;
        if (settingsManager == null) {
            finish();
            return;
        }
        settingsManager.setLightNavigationBar(this, true);
        SettingsManager settingsManager2 = this.mSettingsManager;
        String str = SettingsManager.KEY_SCENE_MODE;
        this.mCurrentScene = settingsManager2.getValueIndex(str);
        this.mEntries = this.mSettingsManager.getEntries(str);
        this.mThumbnails = this.mSettingsManager.getResource(str, 0);
        this.mNumElement = this.mThumbnails.length;
        int i = this.mNumElement;
        int i2 = this.mElemPerPage;
        int i3 = i / i2;
        if (i % i2 != 0) {
            i3++;
        }
        this.mNumPage = i3;
        this.mAdapter = new MyPagerAdapter(this);
        this.mPager = (ViewPager) findViewById(C0905R.C0907id.pager);
        this.mPager.setOverScrollMode(2);
        this.mPager.setAdapter(this.mAdapter);
        this.mCloseButton = findViewById(C0905R.C0907id.close_button);
        this.mCloseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SceneModeActivity.this.finish();
            }
        });
        int count = this.mAdapter.getCount();
        this.mDotsView = (DotsView) findViewById(C0905R.C0907id.page_indicator);
        this.mPager.setCurrentItem(this.mCurrentScene / this.mElemPerPage);
        this.mDotsView.update(this.mCurrentScene / this.mElemPerPage, 0.0f);
        if (count > 1) {
            this.mDotsView.setItems(new PageItems(count));
            this.mPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
                public void onPageScrolled(int i, float f, int i2) {
                    SceneModeActivity.this.mDotsView.update(i, f);
                }
            });
        } else {
            this.mDotsView.setVisibility(8);
        }
        this.mButton = (RotateImageView) findViewById(C0905R.C0907id.setting_button);
        this.mButton.setVisibility(4);
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(SceneModeActivity.this.getBaseContext(), SettingsActivity.class);
                intent.putExtra(CameraUtil.KEY_IS_SECURE_CAMERA, booleanExtra);
                SceneModeActivity.this.startActivity(intent);
                SceneModeActivity.this.finish();
            }
        });
    }

    public int getElmentPerPage() {
        return this.mElemPerPage;
    }

    public int getNumberOfPage() {
        return this.mNumPage;
    }

    public int getNumberOfElement() {
        return this.mNumElement;
    }

    public int getCurrentPage() {
        return this.mPager.getCurrentItem();
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public int[] getThumbnails() {
        return this.mThumbnails;
    }

    public int getCurrentScene() {
        return this.mCurrentScene;
    }

    private void setShowInLockScreen() {
        Window window = getWindow();
        LayoutParams attributes = window.getAttributes();
        attributes.flags |= 524288;
        window.setAttributes(attributes);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        finish();
    }
}
