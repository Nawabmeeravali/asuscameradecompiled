package com.android.camera;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import org.codeaurora.snapcam.C0905R;

public class PermissionsActivity extends Activity {
    private static int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "PermissionsActivity";
    private boolean mCriticalPermissionDenied;
    private boolean mFlagHasCameraPermission;
    private boolean mFlagHasMicrophonePermission;
    private boolean mFlagHasStoragePermission;
    private int mIndexPermissionRequestCamera;
    private int mIndexPermissionRequestLocation;
    private int mIndexPermissionRequestMicrophone;
    private int mIndexPermissionRequestStorageRead;
    private int mIndexPermissionRequestStorageWrite;
    private Intent mIntent;
    private boolean mIsReturnResult;
    private int mNumPermissionsToRequest;
    private boolean mShouldRequestCameraPermission;
    private boolean mShouldRequestLocationPermission;
    private boolean mShouldRequestMicrophonePermission;
    private boolean mShouldRequestStoragePermission;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mIntent = getIntent();
        this.mIsReturnResult = false;
        if (this.mCriticalPermissionDenied || this.mIsReturnResult) {
            this.mCriticalPermissionDenied = false;
            return;
        }
        this.mNumPermissionsToRequest = 0;
        checkPermissions();
    }

    private void checkPermissions() {
        if (checkSelfPermission("android.permission.CAMERA") != 0) {
            this.mNumPermissionsToRequest++;
            this.mShouldRequestCameraPermission = true;
        } else {
            this.mFlagHasCameraPermission = true;
        }
        if (checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
            this.mNumPermissionsToRequest++;
            this.mShouldRequestMicrophonePermission = true;
        } else {
            this.mFlagHasMicrophonePermission = true;
        }
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0 && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0) {
            this.mFlagHasStoragePermission = true;
        } else {
            this.mNumPermissionsToRequest += 2;
            this.mShouldRequestStoragePermission = true;
        }
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0) {
            this.mNumPermissionsToRequest++;
            this.mShouldRequestLocationPermission = true;
        }
        if (this.mNumPermissionsToRequest != 0) {
            buildPermissionsRequest();
        } else {
            handlePermissionsSuccess();
        }
    }

    private void buildPermissionsRequest() {
        String[] strArr = new String[this.mNumPermissionsToRequest];
        int i = 0;
        if (this.mShouldRequestCameraPermission) {
            strArr[0] = "android.permission.CAMERA";
            this.mIndexPermissionRequestCamera = 0;
            i = 1;
        }
        if (this.mShouldRequestMicrophonePermission) {
            strArr[i] = "android.permission.RECORD_AUDIO";
            this.mIndexPermissionRequestMicrophone = i;
            i++;
        }
        if (this.mShouldRequestStoragePermission) {
            strArr[i] = "android.permission.WRITE_EXTERNAL_STORAGE";
            this.mIndexPermissionRequestStorageWrite = i;
            int i2 = i + 1;
            strArr[i2] = "android.permission.READ_EXTERNAL_STORAGE";
            this.mIndexPermissionRequestStorageRead = i2;
            i = i2 + 1;
        }
        if (this.mShouldRequestLocationPermission) {
            strArr[i] = "android.permission.ACCESS_FINE_LOCATION";
            this.mIndexPermissionRequestLocation = i;
        }
        requestPermissions(strArr, PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (this.mShouldRequestCameraPermission) {
            int length = iArr.length;
            int i2 = this.mIndexPermissionRequestCamera;
            if (length < i2 + 1 || iArr[i2] != 0) {
                this.mCriticalPermissionDenied = true;
            } else {
                this.mFlagHasCameraPermission = true;
            }
        }
        if (this.mShouldRequestMicrophonePermission) {
            int length2 = iArr.length;
            int i3 = this.mIndexPermissionRequestMicrophone;
            if (length2 < i3 + 1 || iArr[i3] != 0) {
                this.mCriticalPermissionDenied = true;
            } else {
                this.mFlagHasMicrophonePermission = true;
            }
        }
        if (this.mShouldRequestStoragePermission) {
            int length3 = iArr.length;
            int i4 = this.mIndexPermissionRequestStorageRead;
            if (length3 >= i4 + 1 && iArr[this.mIndexPermissionRequestStorageWrite] == 0 && iArr[i4] == 0) {
                this.mFlagHasStoragePermission = true;
            } else {
                this.mCriticalPermissionDenied = true;
            }
        }
        if (this.mShouldRequestLocationPermission) {
            int length4 = iArr.length;
            int i5 = this.mIndexPermissionRequestLocation;
            if (length4 >= i5 + 1) {
                int i6 = iArr[i5];
            }
        }
        if (this.mFlagHasCameraPermission && this.mFlagHasMicrophonePermission && this.mFlagHasStoragePermission) {
            handlePermissionsSuccess();
        } else if (this.mCriticalPermissionDenied) {
            handlePermissionsFailure();
        }
    }

    private void handlePermissionsSuccess() {
        if (this.mIntent != null) {
            setRequestPermissionShow();
            this.mIsReturnResult = true;
            this.mIntent.setClass(this, CameraActivity.class);
            this.mIntent.addFlags(33554432);
            startActivity(this.mIntent);
            finish();
            return;
        }
        this.mIsReturnResult = false;
        startActivity(new Intent(this, CameraActivity.class));
        finish();
    }

    private void handlePermissionsFailure() {
        new Builder(this).setTitle(getResources().getString(C0905R.string.camera_error_title)).setMessage(getResources().getString(C0905R.string.error_permissions)).setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == 4) {
                    PermissionsActivity.this.finish();
                }
                return true;
            }
        }).setPositiveButton(getResources().getString(C0905R.string.dialog_dismiss), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PermissionsActivity.this.finish();
            }
        }).show();
    }

    private void setRequestPermissionShow() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = CameraSettings.KEY_REQUEST_PERMISSION;
        if (!defaultSharedPreferences.getBoolean(str, false)) {
            Editor edit = defaultSharedPreferences.edit();
            edit.putBoolean(str, true);
            edit.apply();
        }
    }
}
