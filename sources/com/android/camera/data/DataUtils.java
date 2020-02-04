package com.android.camera.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class DataUtils {
    public static String getPathFromURI(ContentResolver contentResolver, Uri uri) {
        String str = "_data";
        Cursor query = contentResolver.query(uri, new String[]{str}, null, null, null);
        if (query == null) {
            return null;
        }
        try {
            int columnIndexOrThrow = query.getColumnIndexOrThrow(str);
            if (!query.moveToFirst()) {
                return null;
            }
            String string = query.getString(columnIndexOrThrow);
            query.close();
            return string;
        } finally {
            query.close();
        }
    }
}
