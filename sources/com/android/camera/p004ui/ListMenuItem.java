package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera.IconListPreference;
import com.android.camera.ListPreference;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ListMenuItem */
public class ListMenuItem extends RelativeLayout {
    private static final String TAG = "ListMenuItem";
    private TextView mEntry;
    private ImageView mIcon;
    protected int mIndex;
    private Listener mListener;
    protected String mOverrideValue;
    protected ListPreference mPreference;
    protected TextView mTitle;

    /* renamed from: com.android.camera.ui.ListMenuItem$Listener */
    public interface Listener {
        void onSettingChanged(ListPreference listPreference);
    }

    public ListMenuItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEntry = (TextView) findViewById(C0905R.C0907id.current_setting);
        this.mIcon = (ImageView) findViewById(C0905R.C0907id.list_image);
    }

    /* access modifiers changed from: protected */
    public void setTitle(ListPreference listPreference) {
        this.mTitle = (TextView) findViewById(C0905R.C0907id.title);
        this.mTitle.setText(listPreference.getTitle());
    }

    /* access modifiers changed from: protected */
    public void setIcon(ListPreference listPreference) {
        if (listPreference instanceof IconListPreference) {
            this.mIcon.setImageResource(((IconListPreference) listPreference).getSingleIcon());
        }
    }

    public void initialize(ListPreference listPreference) {
        setTitle(listPreference);
        if (listPreference != null) {
            setIcon(listPreference);
            this.mPreference = listPreference;
            reloadPreference();
        }
    }

    /* access modifiers changed from: protected */
    public void updateView() {
        String str = this.mOverrideValue;
        if (str == null) {
            this.mEntry.setText(this.mPreference.getEntry());
            return;
        }
        int findIndexOfValue = this.mPreference.findIndexOfValue(str);
        if (findIndexOfValue != -1) {
            this.mEntry.setText(this.mPreference.getEntries()[findIndexOfValue]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Fail to find override value=");
        sb.append(this.mOverrideValue);
        Log.e(TAG, sb.toString());
        this.mPreference.print();
    }

    public void reloadPreference() {
        ListPreference listPreference = this.mPreference;
        this.mIndex = listPreference.findIndexOfValue(listPreference.getValue());
        updateView();
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public void overrideSettings(String str) {
        this.mOverrideValue = str;
        updateView();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        List text = accessibilityEvent.getText();
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPreference.getTitle());
        sb.append(this.mPreference.getEntry());
        text.add(sb.toString());
        return true;
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.3f);
        }
        TextView textView = this.mTitle;
        if (textView != null) {
            textView.setEnabled(z);
            if (z) {
                setAlpha(1.0f);
            } else {
                setAlpha(0.3f);
            }
        }
        TextView textView2 = this.mEntry;
        if (textView2 != null) {
            textView2.setEnabled(z);
            if (z) {
                setAlpha(1.0f);
            } else {
                setAlpha(0.3f);
            }
        }
    }

    public void setEnabled(boolean z, String str) {
        super.setEnabled(z);
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.3f);
        }
        TextView textView = this.mTitle;
        if (textView != null) {
            textView.setEnabled(z);
            if (z) {
                setAlpha(1.0f);
            } else {
                setAlpha(0.3f);
            }
        }
        TextView textView2 = this.mEntry;
        if (textView2 != null) {
            textView2.setEnabled(z);
            if (z) {
                setAlpha(1.0f);
            } else {
                setAlpha(0.3f);
            }
        }
    }
}
