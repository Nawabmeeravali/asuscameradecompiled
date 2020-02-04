package android.support.p000v4.media;

import android.support.annotation.RequiresApi;
import java.lang.reflect.Constructor;
import java.util.List;

@RequiresApi(21)
/* renamed from: android.support.v4.media.ParceledListSliceAdapterApi21 */
class ParceledListSliceAdapterApi21 {
    private static Constructor sConstructor;

    static {
        try {
            sConstructor = Class.forName("android.content.pm.ParceledListSlice").getConstructor(new Class[]{List.class});
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private ParceledListSliceAdapterApi21() {
    }
}
