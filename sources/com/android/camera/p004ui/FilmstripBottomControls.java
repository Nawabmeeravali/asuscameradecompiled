package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.android.camera.CameraActivity.OnActionBarVisibilityListener;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.FilmstripBottomControls */
public class FilmstripBottomControls extends RelativeLayout implements OnActionBarVisibilityListener {
    private ImageButton mEditButton;
    /* access modifiers changed from: private */
    public BottomControlsListener mListener;
    private ImageButton mTinyPlanetButton;
    private ImageButton mViewPhotoSphereButton;

    /* renamed from: com.android.camera.ui.FilmstripBottomControls$BottomControlsListener */
    public interface BottomControlsListener {
        void onEdit();

        void onTinyPlanet();

        void onViewPhotoSphere();
    }

    public FilmstripBottomControls(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEditButton = (ImageButton) findViewById(C0905R.C0907id.filmstrip_bottom_control_edit);
        this.mEditButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FilmstripBottomControls.this.mListener != null) {
                    FilmstripBottomControls.this.mListener.onEdit();
                }
            }
        });
        this.mViewPhotoSphereButton = (ImageButton) findViewById(C0905R.C0907id.filmstrip_bottom_control_panorama);
        this.mViewPhotoSphereButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FilmstripBottomControls.this.mListener != null) {
                    FilmstripBottomControls.this.mListener.onViewPhotoSphere();
                }
            }
        });
        this.mTinyPlanetButton = (ImageButton) findViewById(C0905R.C0907id.filmstrip_bottom_control_tiny_planet);
        this.mTinyPlanetButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (FilmstripBottomControls.this.mListener != null) {
                    FilmstripBottomControls.this.mListener.onTinyPlanet();
                }
            }
        });
    }

    public void setListener(BottomControlsListener bottomControlsListener) {
        this.mListener = bottomControlsListener;
    }

    public void setEditButtonVisibility(boolean z) {
        setVisibility(this.mEditButton, z);
    }

    public void setViewPhotoSphereButtonVisibility(boolean z) {
        setVisibility(this.mViewPhotoSphereButton, z);
    }

    public void setTinyPlanetButtonVisibility(boolean z) {
        setVisibility(this.mTinyPlanetButton, z);
    }

    private static void setVisibility(final View view, final boolean z) {
        view.post(new Runnable() {
            public void run() {
                view.setVisibility(z ? 0 : 4);
            }
        });
    }

    public void onActionBarVisibilityChanged(boolean z) {
        setVisibility(z ? 0 : 4);
    }
}
