package com.android.camera.mpo;

import java.util.HashMap;
import java.util.Map;

public class MpoIfdData {
    public static final byte[] MP_FORMAT_VER_VALUE = {48, 49, 48, 48};
    public static final int TYPE_MP_ATTRIB_IFD = 2;
    public static final int TYPE_MP_INDEX_IFD = 1;
    private final int mIfdId;
    private int mOffsetToNextIfd = 0;
    private final Map<Short, MpoTag> mTags = new HashMap();

    public MpoIfdData(int i) {
        this.mIfdId = i;
    }

    /* access modifiers changed from: protected */
    public MpoTag[] getAllTags() {
        return (MpoTag[]) this.mTags.values().toArray(new MpoTag[this.mTags.size()]);
    }

    /* access modifiers changed from: protected */
    public MpoTag getTag(short s) {
        return (MpoTag) this.mTags.get(Short.valueOf(s));
    }

    /* access modifiers changed from: protected */
    public MpoTag setTag(MpoTag mpoTag) {
        mpoTag.setIfd(this.mIfdId);
        return (MpoTag) this.mTags.put(Short.valueOf(mpoTag.getTagId()), mpoTag);
    }

    /* access modifiers changed from: protected */
    public int getTagCount() {
        return this.mTags.size();
    }

    /* access modifiers changed from: protected */
    public void setOffsetToNextIfd(int i) {
        this.mOffsetToNextIfd = i;
    }

    /* access modifiers changed from: protected */
    public int getOffsetToNextIfd() {
        return this.mOffsetToNextIfd;
    }

    public boolean equals(Object obj) {
        MpoTag[] allTags;
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof MpoIfdData)) {
            MpoIfdData mpoIfdData = (MpoIfdData) obj;
            if (mpoIfdData.getTagCount() == getTagCount()) {
                for (MpoTag mpoTag : mpoIfdData.getAllTags()) {
                    if (!mpoTag.equals((MpoTag) this.mTags.get(Short.valueOf(mpoTag.getTagId())))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
