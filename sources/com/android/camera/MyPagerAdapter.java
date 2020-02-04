package com.android.camera;

import android.support.p000v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import org.codeaurora.snapcam.C0905R;

/* compiled from: SceneModeActivity */
class MyPagerAdapter extends PagerAdapter {
    /* access modifiers changed from: private */
    public SceneModeActivity mActivity;
    private ViewGroup mRootView;

    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
    }

    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    public MyPagerAdapter(SceneModeActivity sceneModeActivity) {
        this.mActivity = sceneModeActivity;
    }

    public Object instantiateItem(ViewGroup viewGroup, int i) {
        this.mRootView = (ViewGroup) this.mActivity.getLayoutInflater().inflate(C0905R.layout.scene_mode_grid, null);
        GridView gridView = (GridView) this.mRootView.findViewById(C0905R.C0907id.grid);
        gridView.setAdapter(new GridAdapter(this.mActivity, i));
        viewGroup.addView(this.mRootView);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                int currentPage = (MyPagerAdapter.this.mActivity.getCurrentPage() * MyPagerAdapter.this.mActivity.getElmentPerPage()) + i;
                for (int i2 = 0; i2 < adapterView.getChildCount(); i2++) {
                    View childAt = adapterView.getChildAt(i2);
                    if (childAt != null) {
                        childAt.setBackground(null);
                    }
                }
                view.setBackgroundResource(C0905R.C0906drawable.scene_mode_view_border_selected);
                SettingsManager.getInstance().setValueIndex(SettingsManager.KEY_SCENE_MODE, currentPage);
                MyPagerAdapter.this.mActivity.finish();
            }
        });
        return this.mRootView;
    }

    public int getCount() {
        return this.mActivity.getNumberOfPage();
    }
}
