package com.android.camera;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import java.util.List;
import org.codeaurora.snapcam.C0905R;

public class IconListPreference extends ListPreference {
    private int[] mIconIds;
    private int[] mImageIds;
    private int[] mLargeIconIds;
    private int mSingleIconId;
    private int[] mThumbnailIds;
    private boolean mUseSingleIcon;

    public IconListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0905R.styleable.IconListPreference, 0, 0);
        Resources resources = context.getResources();
        this.mSingleIconId = obtainStyledAttributes.getResourceId(3, 0);
        this.mIconIds = getIds(resources, obtainStyledAttributes.getResourceId(0, 0));
        this.mLargeIconIds = getIds(resources, obtainStyledAttributes.getResourceId(2, 0));
        this.mImageIds = getIds(resources, obtainStyledAttributes.getResourceId(1, 0));
        this.mThumbnailIds = getIds(resources, obtainStyledAttributes.getResourceId(4, 0));
        obtainStyledAttributes.recycle();
    }

    public int getSingleIcon() {
        return this.mSingleIconId;
    }

    public int[] getIconIds() {
        return this.mIconIds;
    }

    public int[] getLargeIconIds() {
        return this.mLargeIconIds;
    }

    public int[] getThumbnailIds() {
        return this.mThumbnailIds;
    }

    public int[] getImageIds() {
        return this.mImageIds;
    }

    public boolean getUseSingleIcon() {
        return this.mUseSingleIcon;
    }

    public void setIconIds(int[] iArr) {
        this.mIconIds = iArr;
    }

    public void setLargeIconIds(int[] iArr) {
        this.mLargeIconIds = iArr;
    }

    public void setThumbnailIds(int[] iArr) {
        this.mThumbnailIds = iArr;
    }

    public void setUseSingleIcon(boolean z) {
        this.mUseSingleIcon = z;
    }

    private int[] getIds(Resources resources, int i) {
        if (i == 0) {
            return null;
        }
        TypedArray obtainTypedArray = resources.obtainTypedArray(i);
        int length = obtainTypedArray.length();
        int[] iArr = new int[length];
        for (int i2 = 0; i2 < length; i2++) {
            iArr[i2] = obtainTypedArray.getResourceId(i2, 0);
        }
        obtainTypedArray.recycle();
        return iArr;
    }

    public void filterUnsupported(List<String> list) {
        CharSequence[] entryValues = getEntryValues();
        IntArray intArray = new IntArray();
        IntArray intArray2 = new IntArray();
        IntArray intArray3 = new IntArray();
        IntArray intArray4 = new IntArray();
        int length = entryValues.length;
        for (int i = 0; i < length; i++) {
            if (list.indexOf(entryValues[i].toString()) >= 0) {
                int[] iArr = this.mIconIds;
                if (iArr != null) {
                    intArray.add(iArr[i]);
                }
                int[] iArr2 = this.mLargeIconIds;
                if (iArr2 != null) {
                    intArray2.add(iArr2[i]);
                }
                int[] iArr3 = this.mImageIds;
                if (iArr3 != null) {
                    intArray3.add(iArr3[i]);
                }
                int[] iArr4 = this.mThumbnailIds;
                if (iArr4 != null) {
                    intArray4.add(iArr4[i]);
                }
            }
        }
        if (this.mIconIds != null) {
            this.mIconIds = intArray.toArray(new int[intArray.size()]);
        }
        if (this.mLargeIconIds != null) {
            this.mLargeIconIds = intArray2.toArray(new int[intArray2.size()]);
        }
        if (this.mImageIds != null) {
            this.mImageIds = intArray3.toArray(new int[intArray3.size()]);
        }
        if (this.mThumbnailIds != null) {
            this.mThumbnailIds = intArray4.toArray(new int[intArray4.size()]);
        }
        super.filterUnsupported(list);
    }
}
