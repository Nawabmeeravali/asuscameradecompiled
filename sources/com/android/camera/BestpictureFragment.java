package com.android.camera;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import org.codeaurora.snapcam.C0905R;

public class BestpictureFragment extends Fragment {
    public static final String PARAM_IMAGE_NUM = "image_num";
    private static final String TAG = "BestpictureFilter";
    /* access modifiers changed from: private */
    public ImageItems mImageItems;
    /* access modifiers changed from: private */
    public int mImageNum;
    private ImageView mImageView;
    private ImageView mPictureSelectButton;

    public static BestpictureFragment create(int i, ImageItems imageItems) {
        BestpictureFragment bestpictureFragment = new BestpictureFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_IMAGE_NUM, i);
        bestpictureFragment.setArguments(bundle);
        return bestpictureFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mImageNum = getArguments().getInt(PARAM_IMAGE_NUM);
        this.mImageItems = ((BestpictureActivity) getActivity()).getImageItems();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ViewGroup viewGroup2 = (ViewGroup) layoutInflater.inflate(C0905R.layout.bestpicture_page, viewGroup, false);
        this.mImageView = (ImageView) viewGroup2.findViewById(C0905R.C0907id.image_view);
        this.mPictureSelectButton = (ImageView) viewGroup2.findViewById(C0905R.C0907id.picture_select);
        if (this.mImageItems != null) {
            initSelectButton();
            this.mImageView.setImageBitmap(this.mImageItems.getBitmap(this.mImageNum));
            viewGroup2.findViewById(C0905R.C0907id.picture_select).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    BestpictureFragment.this.mImageItems.toggleImageSelection(BestpictureFragment.this.mImageNum);
                    BestpictureFragment.this.initSelectButton();
                }
            });
        }
        return viewGroup2;
    }

    /* access modifiers changed from: private */
    public void initSelectButton() {
        if (this.mImageItems.isChosen(this.mImageNum)) {
            this.mPictureSelectButton.setBackground(getResources().getDrawable(C0905R.C0906drawable.pick_the_best_photo_selected, null));
        } else {
            this.mPictureSelectButton.setBackground(getResources().getDrawable(C0905R.C0906drawable.pick_the_best_photo_unselected, null));
        }
    }

    public void onDestroy() {
        this.mImageItems = null;
        super.onDestroy();
    }
}
