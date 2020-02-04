package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.android.camera.IconListPreference;
import com.android.camera.ListPreference;
import com.asus.scenedetectlib.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.ListSubMenu */
public class ListSubMenu extends ListView implements OnItemClickListener {
    private static final String TAG = "ListPrefSettingPopup";
    private Listener mListener;
    private ListPreference mPreference;

    /* renamed from: mY */
    private int f84mY;

    /* renamed from: com.android.camera.ui.ListSubMenu$ListPrefSettingAdapter */
    private class ListPrefSettingAdapter extends SimpleAdapter {
        ListPrefSettingAdapter(Context context, List<? extends Map<String, ?>> list, int i, String[] strArr, int[] iArr) {
            super(context, list, i, strArr, iArr);
        }

        public void setViewImage(ImageView imageView, String str) {
            if (BuildConfig.FLAVOR.equals(str)) {
                imageView.setVisibility(8);
            } else {
                super.setViewImage(imageView, str);
            }
        }
    }

    /* renamed from: com.android.camera.ui.ListSubMenu$Listener */
    public interface Listener {
        void onListPrefChanged(ListPreference listPreference);
    }

    public ListSubMenu(Context context, int i) {
        super(context);
    }

    public ListSubMenu(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(ListPreference listPreference, int i) {
        int[] iArr;
        this.mPreference = listPreference;
        Context context = getContext();
        CharSequence[] entries = this.mPreference.getEntries();
        if (listPreference instanceof IconListPreference) {
            iArr = ((IconListPreference) this.mPreference).getImageIds();
            if (iArr == null) {
                iArr = ((IconListPreference) this.mPreference).getLargeIconIds();
            }
        } else {
            iArr = null;
        }
        this.f84mY = i;
        ArrayList arrayList = new ArrayList();
        int i2 = 0;
        while (true) {
            String str = "image";
            String str2 = "text";
            if (i2 < entries.length) {
                HashMap hashMap = new HashMap();
                hashMap.put(str2, entries[i2].toString());
                if (iArr != null) {
                    hashMap.put(str, Integer.valueOf(iArr[i2]));
                }
                arrayList.add(hashMap);
                i2++;
            } else {
                ListPrefSettingAdapter listPrefSettingAdapter = new ListPrefSettingAdapter(context, arrayList, C0905R.layout.list_sub_menu_item, new String[]{str2, str}, new int[]{C0905R.C0907id.text, C0905R.C0907id.image});
                setAdapter(listPrefSettingAdapter);
                setOnItemClickListener(this);
                reloadPreference();
                return;
            }
        }
    }

    public void reloadPreference() {
        ListPreference listPreference = this.mPreference;
        int findIndexOfValue = listPreference.findIndexOfValue(listPreference.getValue());
        if (findIndexOfValue != -1) {
            setItemChecked(findIndexOfValue, true);
            return;
        }
        Log.e(TAG, "Invalid preference value.");
        this.mPreference.print();
    }

    public void setSettingChangedListener(Listener listener) {
        this.mListener = listener;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        this.mPreference.setValueIndex(i);
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onListPrefChanged(this.mPreference);
        }
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
    }

    public int getPreCalculatedHeight() {
        int count = getAdapter().getCount();
        return (((int) getContext().getResources().getDimension(C0905R.dimen.setting_row_height)) * count) + ((count - 1) * getDividerHeight());
    }

    public int getYBase() {
        return this.f84mY;
    }
}
