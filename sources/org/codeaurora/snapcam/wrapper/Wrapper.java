package org.codeaurora.snapcam.wrapper;

import android.os.SystemProperties;
import android.util.Log;
import com.asus.scenedetectlib.BuildConfig;
import java.lang.reflect.Field;

public class Wrapper {
    protected static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.camera.wrapper.debug", false);

    protected static int getFieldValue(Field field, int i) {
        if (field == null) {
            return i;
        }
        try {
            return ((Integer) field.get(null)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    protected static String getFieldValue(Field field, String str) {
        if (field != null) {
            try {
                return (String) field.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    protected static Field getField(Class<?> cls, String str) {
        StringBuilder sb = new StringBuilder();
        String str2 = "getField:";
        sb.append(str2);
        sb.append(cls);
        sb.append(" field:");
        sb.append(str);
        String str3 = "Wrapper";
        Log.d(str3, sb.toString());
        Field field = null;
        if (DEBUG) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(BuildConfig.FLAVOR);
            sb2.append(cls);
            sb2.append(" no ");
            sb2.append(str);
            Log.e(str3, sb2.toString());
            return null;
        }
        try {
            field = cls.getField(str);
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str2);
            sb3.append(cls);
            sb3.append(" ");
            sb3.append(str);
            Log.d(str3, sb3.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return field;
    }
}
