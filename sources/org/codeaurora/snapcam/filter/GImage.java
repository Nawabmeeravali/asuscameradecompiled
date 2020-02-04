package org.codeaurora.snapcam.filter;

import android.util.Base64;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;

public class GImage {
    public static final String NAMESPACE_URL = "http://ns.google.com/photos/1.0/image/";
    public static final String PREFIX = "GImage";
    public static final String PROPERTY_DATA = "Data";
    public static final String PROPERTY_MIME = "Mime";
    private String mData;
    private String mMime = "image/jpeg";

    static {
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace(NAMESPACE_URL, PREFIX);
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    public GImage(byte[] bArr, String str) {
        this.mData = Base64.encodeToString(bArr, 0);
        this.mMime = str;
    }

    public String getMime() {
        return this.mMime;
    }

    public String getData() {
        return this.mData;
    }
}
