package com.android.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ProxyLauncher extends Activity {
    public static final int RESULT_USER_CANCELED = -2;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            startActivityForResult((Intent) getIntent().getParcelableExtra("android.intent.extra.INTENT"), 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == 0) {
            i2 = -2;
        }
        setResult(i2, intent);
        finish();
    }
}
