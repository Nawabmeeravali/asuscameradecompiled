package com.android.camera.mpo;

import com.adobe.xmp.options.PropertyOptions;
import java.util.ArrayList;
import java.util.List;

public class MpoData {
    private ArrayList<MpoImageData> mAuxiliaryImages = new ArrayList<>();
    private MpoImageData mPrimaryMpoImage;

    public void setPrimaryMpoImage(MpoImageData mpoImageData) {
        this.mPrimaryMpoImage = mpoImageData;
        addDefaultAttribIfdTags(this.mPrimaryMpoImage, 1);
        addDefaultIndexIfdTags();
    }

    public void addAuxiliaryMpoImage(MpoImageData mpoImageData) {
        this.mAuxiliaryImages.add(mpoImageData);
        addDefaultAttribIfdTags(mpoImageData, getAuxiliaryImageCount() + (this.mPrimaryMpoImage == null ? 0 : 1));
    }

    public boolean removeAuxiliaryMpoImage(MpoImageData mpoImageData) {
        return this.mAuxiliaryImages.remove(mpoImageData);
    }

    public MpoImageData getPrimaryMpoImage() {
        return this.mPrimaryMpoImage;
    }

    public List<MpoImageData> getAuxiliaryMpoImages() {
        return this.mAuxiliaryImages;
    }

    public int getAuxiliaryImageCount() {
        return this.mAuxiliaryImages.size();
    }

    public void addDefaultAttribIfdTags(MpoImageData mpoImageData, int i) {
        MpoTag mpoTag = new MpoTag((short) MpoInterface.TAG_MP_FORMAT_VERSION, 7, 4, 2, true);
        mpoTag.setValue(MpoIfdData.MP_FORMAT_VER_VALUE);
        mpoImageData.addTag(mpoTag);
        MpoTag mpoTag2 = new MpoTag((short) MpoInterface.TAG_IMAGE_NUMBER, 4, 1, 2, false);
        mpoTag2.setValue(i);
        mpoImageData.addTag(mpoTag2);
    }

    public void addDefaultIndexIfdTags() {
        if (this.mPrimaryMpoImage == null) {
            throw new IllegalArgumentException("Primary Mpo Image has not been set");
        } else if (getAuxiliaryImageCount() != 0) {
            if (this.mPrimaryMpoImage.getTag((short) MpoInterface.TAG_MP_FORMAT_VERSION, 1) == null) {
                MpoTag mpoTag = new MpoTag((short) MpoInterface.TAG_MP_FORMAT_VERSION, 7, 4, 1, true);
                mpoTag.setValue(MpoIfdData.MP_FORMAT_VER_VALUE);
                this.mPrimaryMpoImage.addTag(mpoTag);
            }
            MpoTag tag = this.mPrimaryMpoImage.getTag((short) MpoInterface.TAG_NUM_IMAGES, 1);
            if (tag == null) {
                tag = new MpoTag((short) MpoInterface.TAG_NUM_IMAGES, 4, 1, 1, false);
            }
            tag.setValue(getAuxiliaryImageCount() + 1);
            this.mPrimaryMpoImage.addTag(tag);
            MpoTag mpoTag2 = new MpoTag((short) MpoInterface.TAG_MP_ENTRY, 7, 0, 1, false);
            ArrayList arrayList = new ArrayList(getAuxiliaryImageCount() + 1);
            arrayList.add(new MpEntry());
            for (int i = 0; i < getAuxiliaryImageCount(); i++) {
                arrayList.add(new MpEntry());
            }
            mpoTag2.setValue(arrayList);
            this.mPrimaryMpoImage.addTag(mpoTag2);
        } else {
            throw new IllegalArgumentException("No auxiliary images have been added");
        }
    }

    public void updateAllTags() {
        updateAttribIfdTags();
        updateIndexIfdTags();
    }

    private void updateIndexIfdTags() {
        if (this.mPrimaryMpoImage == null) {
            throw new IllegalArgumentException("Primary Mpo Image has not been set");
        } else if (getAuxiliaryImageCount() != 0) {
            MpoTag tag = this.mPrimaryMpoImage.getTag((short) MpoInterface.TAG_NUM_IMAGES, 1);
            if (tag == null) {
                tag = new MpoTag((short) MpoInterface.TAG_NUM_IMAGES, 4, 1, 1, false);
            }
            tag.setValue(getAuxiliaryImageCount() + 1);
            this.mPrimaryMpoImage.addTag(tag);
            MpoTag mpoTag = new MpoTag((short) MpoInterface.TAG_MP_ENTRY, 7, 0, 1, false);
            ArrayList arrayList = new ArrayList(getAuxiliaryImageCount() + 1);
            arrayList.add(new MpEntry(PropertyOptions.DELETE_EXISTING, this.mPrimaryMpoImage.calculateImageSize(), 0));
            int calculateImageSize = this.mPrimaryMpoImage.calculateImageSize() + 0;
            for (MpoImageData calculateImageSize2 : getAuxiliaryMpoImages()) {
                int calculateImageSize3 = calculateImageSize2.calculateImageSize();
                arrayList.add(new MpEntry(131074, calculateImageSize3, calculateImageSize));
                calculateImageSize += calculateImageSize3;
            }
            mpoTag.setValue(arrayList);
            this.mPrimaryMpoImage.addTag(mpoTag);
        } else {
            throw new IllegalArgumentException("No auxiliary images have been added");
        }
    }

    private void updateAttribIfdTags() {
        if (this.mPrimaryMpoImage == null) {
            throw new IllegalArgumentException("Primary Mpo Image has not been set");
        } else if (getAuxiliaryImageCount() != 0) {
            MpoTag mpoTag = new MpoTag((short) MpoInterface.TAG_IMAGE_NUMBER, 4, 1, 2, false);
            mpoTag.setValue(4294967295L);
            this.mPrimaryMpoImage.addTag(mpoTag);
            int i = 1;
            for (MpoImageData mpoImageData : getAuxiliaryMpoImages()) {
                MpoTag mpoTag2 = new MpoTag((short) MpoInterface.TAG_IMAGE_NUMBER, 4, 1, 2, false);
                int i2 = i + 1;
                mpoTag2.setValue(i);
                mpoImageData.addTag(mpoTag2);
                i = i2;
            }
        } else {
            throw new IllegalArgumentException("No auxiliary images have been added");
        }
    }
}
