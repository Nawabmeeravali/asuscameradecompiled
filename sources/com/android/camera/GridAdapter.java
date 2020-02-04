package com.android.camera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.codeaurora.snapcam.C0905R;

/* compiled from: SceneModeActivity */
class GridAdapter extends BaseAdapter {
    private SceneModeActivity mActivity;
    private LayoutInflater mInflater;
    private int mPage;

    /* compiled from: SceneModeActivity */
    private class ViewHolder {
        public ImageView imageView;
        public TextView textTitle;

        private ViewHolder() {
        }
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public GridAdapter(SceneModeActivity sceneModeActivity, int i) {
        this.mActivity = sceneModeActivity;
        this.mInflater = (LayoutInflater) sceneModeActivity.getSystemService("layout_inflater");
        this.mPage = i;
    }

    public int getCount() {
        int elmentPerPage = this.mActivity.getElmentPerPage();
        return this.mPage == this.mActivity.getNumberOfPage() + -1 ? this.mActivity.getNumberOfElement() - (this.mPage * elmentPerPage) : elmentPerPage;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View view2;
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view2 = this.mInflater.inflate(C0905R.layout.scene_mode_menu_view, viewGroup, false);
            viewHolder.imageView = (ImageView) view2.findViewById(C0905R.C0907id.image);
            viewHolder.textTitle = (TextView) view2.findViewById(C0905R.C0907id.label);
            view2.setTag(viewHolder);
        } else {
            view2 = view;
            viewHolder = (ViewHolder) view.getTag();
        }
        int elmentPerPage = (this.mPage * this.mActivity.getElmentPerPage()) + i;
        viewHolder.imageView.setImageResource(this.mActivity.getThumbnails()[elmentPerPage]);
        viewHolder.textTitle.setText(this.mActivity.getEntries()[i + (this.mPage * this.mActivity.getElmentPerPage())]);
        if (elmentPerPage == this.mActivity.getCurrentScene()) {
            view2.setBackgroundResource(C0905R.C0906drawable.scene_mode_view_border_selected);
        }
        return view2;
    }
}
