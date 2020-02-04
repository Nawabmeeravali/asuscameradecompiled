package com.android.camera;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.p000v4.app.FragmentActivity;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.android.camera.PhotoModule.NamedImages;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.exif.ExifInterface;
import com.android.camera.p004ui.BestPictureActionDialogLayout;
import com.android.camera.p004ui.BestPictureActionDialogLayout.IDialogDataControler;
import com.android.camera.p004ui.DotsView;
import com.android.camera.p004ui.DotsViewItem;
import com.android.camera.p004ui.RotateImageView;
import com.android.camera.p004ui.RotateTextToast;
import com.android.camera.util.CameraUtil;
import com.asus.scenedetectlib.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.codeaurora.snapcam.C0905R;

public class BestpictureActivity extends FragmentActivity {
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static int BESTPICTURE_ACTIVITY_CODE = 11;
    private static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String[] NAMES = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
    public static final int NUM_IMAGES = 10;
    public static final String SECURE_CAMERA_EXTRA = "secure_camera";
    private static final String TAG = "BestpictureActivity";
    /* access modifiers changed from: private */
    public BestpictureActivity mActivity;
    private Builder mBuilder;
    /* access modifiers changed from: private */
    public Dialog mDialog;
    private View mDialogRoot;
    /* access modifiers changed from: private */
    public DotsView mDotsView;
    /* access modifiers changed from: private */
    public String mFilesPath;
    /* access modifiers changed from: private */
    public int mHeight;
    /* access modifiers changed from: private */
    public ImageItems mImageItems;
    private ViewPager mImagePager;
    private PagerAdapter mImagePagerAdapter;
    private ImageLoadingThread mLoadingThread;
    /* access modifiers changed from: private */
    public NamedImages mNamedImages;
    private Uri mPlaceHolderUri;
    /* access modifiers changed from: private */
    public ProgressDialog mProgressDialog;
    private boolean mSecureCamera;
    /* access modifiers changed from: private */
    public int mWidth;
    /* access modifiers changed from: private */
    public CheckBox mshowAgainCheck;

    static class ImageItems implements DotsViewItem {
        private BestpictureActivity mActivity;
        private Bitmap[] mBitmap = new Bitmap[10];
        /* access modifiers changed from: private */
        public boolean[] mChosen = new boolean[10];

        public int getTotalItemNums() {
            return 10;
        }

        public ImageItems(BestpictureActivity bestpictureActivity) {
            int i = 0;
            while (true) {
                boolean[] zArr = this.mChosen;
                if (i < zArr.length) {
                    if (i == 0) {
                        zArr[i] = true;
                    } else {
                        zArr[i] = false;
                    }
                    i++;
                } else {
                    this.mActivity = bestpictureActivity;
                    return;
                }
            }
        }

        public Bitmap getBitmap(int i) {
            return this.mBitmap[i];
        }

        public void setBitmap(int i, Bitmap bitmap) {
            this.mBitmap[i] = bitmap;
        }

        public boolean isChosen(int i) {
            return this.mChosen[i];
        }

        public void toggleImageSelection(int i) {
            boolean[] zArr;
            boolean[] zArr2 = this.mChosen;
            zArr2[i] = !zArr2[i];
            int i2 = 0;
            boolean z = false;
            while (true) {
                zArr = this.mChosen;
                if (i2 >= zArr.length) {
                    break;
                }
                z |= zArr[i2];
                i2++;
            }
            if (!z) {
                zArr[i] = true;
                BestpictureActivity bestpictureActivity = this.mActivity;
                RotateTextToast.makeText((Activity) bestpictureActivity, (CharSequence) bestpictureActivity.getResources().getString(C0905R.string.bestpicture_at_least_one_picture), 0).show();
            }
            this.mActivity.mDotsView.update();
        }
    }

    private class ImageLoadingThread extends Thread {
        private ImageLoadingThread() {
        }

        public void run() {
            int i;
            BestpictureActivity.this.showProgressDialog();
            for (int i2 = 0; i2 < 10; i2++) {
                StringBuilder sb = new StringBuilder();
                sb.append(BestpictureActivity.this.mFilesPath);
                sb.append("/");
                sb.append(BestpictureActivity.NAMES[i2]);
                sb.append(Storage.JPEG_POSTFIX);
                String sb2 = sb.toString();
                Options options = new Options();
                int i3 = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(sb2, options);
                ExifInterface exifInterface = new ExifInterface();
                try {
                    exifInterface.readExif(sb2);
                    i = Exif.getOrientation(exifInterface);
                } catch (IOException unused) {
                    i = 0;
                }
                int i4 = options.outHeight;
                int i5 = options.outWidth;
                if (i4 > BestpictureActivity.this.mHeight || i5 > BestpictureActivity.this.mWidth) {
                    while ((i4 / i3) / 2 > BestpictureActivity.this.mHeight && (i5 / i3) / 2 > BestpictureActivity.this.mWidth) {
                        i3 *= 2;
                    }
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = i3;
                Bitmap decodeFile = BitmapFactory.decodeFile(sb2, options);
                if (i != 0) {
                    Matrix matrix = new Matrix();
                    matrix.setRotate((float) i);
                    decodeFile = Bitmap.createBitmap(decodeFile, 0, 0, decodeFile.getWidth(), decodeFile.getHeight(), matrix, false);
                }
                BestpictureActivity.this.mImageItems.setBitmap(i2, decodeFile);
            }
            BestpictureActivity.this.dismissProgressDialog();
        }
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        public int getCount() {
            return 10;
        }

        public ImagePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public Fragment getItem(int i) {
            while (BestpictureActivity.this.mImageItems.getBitmap(i) == null) {
                try {
                    Thread.sleep(5);
                } catch (Exception unused) {
                }
            }
            return BestpictureFragment.create(i, BestpictureActivity.this.mImageItems);
        }
    }

    private class SaveImageTask extends AsyncTask<String, Void, Void> {
        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
        }

        private SaveImageTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(String... strArr) {
            String str;
            BestpictureActivity.this.mNamedImages.nameNewImage(System.currentTimeMillis());
            NamedEntity nextNameEntity = BestpictureActivity.this.mNamedImages.getNextNameEntity();
            if (nextNameEntity == null) {
                str = null;
            } else {
                str = nextNameEntity.title;
            }
            String generateFilepath = Storage.generateFilepath(str, PhotoModule.PIXEL_FORMAT_JPEG);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(generateFilepath);
                FileInputStream fileInputStream = new FileInputStream(strArr[0]);
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                fileOutputStream.close();
            } catch (Exception unused) {
            }
            BestpictureActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(generateFilepath))));
            return null;
        }
    }

    public ImageItems getImageItems() {
        return this.mImageItems;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mActivity = this;
        Intent intent = getIntent();
        String action = intent.getAction();
        if ("android.media.action.STILL_IMAGE_CAMERA_SECURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action)) {
            this.mSecureCamera = true;
        } else {
            this.mSecureCamera = intent.getBooleanExtra("secure_camera", false);
        }
        if (this.mSecureCamera) {
            Window window = getWindow();
            LayoutParams attributes = window.getAttributes();
            attributes.flags |= 524288;
            window.setAttributes(attributes);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getFilesDir());
        sb.append("/Bestpicture");
        this.mFilesPath = sb.toString();
        setContentView(C0905R.layout.bestpicture_editor);
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        this.mWidth = point.x / 4;
        this.mHeight = point.y / 4;
        this.mNamedImages = new NamedImages();
        this.mImageItems = new ImageItems(this.mActivity);
        this.mDotsView = (DotsView) findViewById(C0905R.C0907id.dots_view);
        this.mDotsView.setItems(this.mImageItems);
        this.mPlaceHolderUri = getIntent().getData();
        this.mImagePager = (ViewPager) findViewById(C0905R.C0907id.bestpicture_pager);
        this.mImagePagerAdapter = new ImagePagerAdapter(getFragmentManager());
        this.mImagePager.setAdapter(this.mImagePagerAdapter);
        this.mImagePager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int i) {
            }

            public void onPageScrolled(int i, float f, int i2) {
                BestpictureActivity.this.mDotsView.update(i, f);
            }
        });
        findViewById(C0905R.C0907id.bestpicture_done).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int i = 0;
                for (boolean z : BestpictureActivity.this.mImageItems.mChosen) {
                    if (z) {
                        i++;
                    }
                }
                if (CameraUtil.loadDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_SAVE)) {
                    BestpictureActivity.this.initSaveDialog(1, i);
                    BestpictureActivity.this.mDialog.show();
                    BestpictureActivity.this.setDialogLayoutPararms();
                    return;
                }
                BestpictureActivity.this.saveImages(i, false);
            }
        });
        findViewById(C0905R.C0907id.delete_best).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (CameraUtil.loadDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE)) {
                    BestpictureActivity.this.initDeleteDialog(1);
                    BestpictureActivity.this.mDialog.show();
                    BestpictureActivity.this.setDialogLayoutPararms();
                    return;
                }
                BestpictureActivity.this.backToViewfinder();
            }
        });
        RotateImageView rotateImageView = (RotateImageView) findViewById(C0905R.C0907id.best_more);
        rotateImageView.setImageBitmap(CameraUtil.adjustPhotoRotation(BitmapFactory.decodeResource(getResources(), C0905R.C0906drawable.more_options), 90));
        rotateImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BestpictureActivity.this.initOverFlow(view);
            }
        });
    }

    /* access modifiers changed from: private */
    public void initOverFlow(View view) {
        View inflate = getLayoutInflater().inflate(C0905R.layout.overflow, null);
        final PopupWindow popupWindow = new PopupWindow(inflate, CameraUtil.dip2px(this, 150.0f), CameraUtil.dip2px(this, 100.0f), true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(view, 53, CameraUtil.dip2px(this, 5.0f), CameraUtil.dip2px(this, 10.0f));
        TextView textView = (TextView) inflate.findViewById(C0905R.C0907id.overflow_item1);
        TextView textView2 = (TextView) inflate.findViewById(C0905R.C0907id.overflow_item2);
        textView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                popupWindow.dismiss();
                BestpictureActivity bestpictureActivity = BestpictureActivity.this;
                bestpictureActivity.saveImages(bestpictureActivity.mImageItems.mChosen.length, true);
            }
        });
        textView2.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                popupWindow.dismiss();
                if (CameraUtil.loadDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE_ALL)) {
                    BestpictureActivity.this.initDeleteAllDialog(1);
                    BestpictureActivity.this.mDialog.show();
                    BestpictureActivity.this.setDialogLayoutPararms();
                    return;
                }
                BestpictureActivity.this.backToViewfinder();
            }
        });
    }

    /* access modifiers changed from: private */
    public void initDeleteDialog(int i) {
        getDialogLayout(i).setDialogDataControler(this.mDialogRoot, new IDialogDataControler() {
            public void doClickOKBtAction() {
            }

            public String getOKButtonString() {
                return BuildConfig.FLAVOR;
            }

            public String getTitleString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_best_dialog_title);
            }

            public String getContentString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_best_dialog_content);
            }

            public String getPositionButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_best_dialog_positive_bt);
            }

            public String getNativeButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_best_dialog_native_bt);
            }

            public void doClickPositionBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
                BestpictureActivity.this.backToViewfinder();
            }

            public void doClickNativeBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
            }
        });
    }

    /* access modifiers changed from: private */
    public void initDeleteAllDialog(int i) {
        getDialogLayout(i).setDialogDataControler(this.mDialogRoot, new IDialogDataControler() {
            public void doClickOKBtAction() {
            }

            public String getOKButtonString() {
                return BuildConfig.FLAVOR;
            }

            public String getTitleString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_all_best_dialog_title);
            }

            public String getContentString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_all_best_dialog_content);
            }

            public String getPositionButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_all_best_dialog_positive_bt);
            }

            public String getNativeButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.delete_all_best_dialog_native_bt);
            }

            public void doClickPositionBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE_ALL, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
                BestpictureActivity.this.backToViewfinder();
            }

            public void doClickNativeBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_DELETE_ALL, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
            }
        });
    }

    /* access modifiers changed from: private */
    public void initSaveDialog(int i, final int i2) {
        getDialogLayout(i).setDialogDataControler(this.mDialogRoot, new IDialogDataControler() {
            public String getOKButtonString() {
                return BuildConfig.FLAVOR;
            }

            public String getTitleString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.save_best_dialog_title);
            }

            public String getContentString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.save_best_dialog_content, new Object[]{Integer.valueOf(i2)});
            }

            public String getPositionButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.save_best_dialog_positive_bt);
            }

            public String getNativeButtonString() {
                return BestpictureActivity.this.getResources().getString(C0905R.string.save_best_dialog_native_bt);
            }

            public void doClickPositionBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_SAVE, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
                BestpictureActivity.this.saveImages(i2, false);
            }

            public void doClickNativeBtAction() {
                if (BestpictureActivity.this.mshowAgainCheck.isChecked()) {
                    CameraUtil.saveDialogShowConfig(BestpictureActivity.this, CameraUtil.KEY_SAVE, false);
                }
                BestpictureActivity.this.mDialog.dismiss();
            }

            public void doClickOKBtAction() {
                BestpictureActivity.this.mDialog.dismiss();
            }
        });
    }

    private BestPictureActionDialogLayout getDialogLayout(int i) {
        this.mBuilder = new Builder(this);
        this.mDialogRoot = LayoutInflater.from(this).inflate(C0905R.layout.bestpicture_action_dialog, null);
        this.mDialogRoot.setTag(Integer.valueOf(i));
        this.mBuilder.setView(this.mDialogRoot);
        this.mDialog = this.mBuilder.create();
        BestPictureActionDialogLayout bestPictureActionDialogLayout = (BestPictureActionDialogLayout) this.mDialogRoot.findViewById(C0905R.C0907id.mlayout);
        this.mshowAgainCheck = (CheckBox) this.mDialogRoot.findViewById(C0905R.C0907id.mcheck);
        return bestPictureActionDialogLayout;
    }

    /* access modifiers changed from: private */
    public void setDialogLayoutPararms() {
        this.mDialog.getWindow().setLayout(CameraUtil.dip2px(this, 320.0f), CameraUtil.dip2px(this, 250.0f));
    }

    /* access modifiers changed from: private */
    public void saveImages(int i, boolean z) {
        int i2 = -1;
        for (int i3 = 0; i3 < this.mImageItems.mChosen.length; i3++) {
            String str = Storage.JPEG_POSTFIX;
            String str2 = "/";
            if (!z) {
                if (this.mImageItems.mChosen[i3]) {
                    if (i2 != -1) {
                        SaveImageTask saveImageTask = new SaveImageTask();
                        StringBuilder sb = new StringBuilder();
                        sb.append(this.mFilesPath);
                        sb.append(str2);
                        sb.append(NAMES[i3]);
                        sb.append(str);
                        saveImageTask.execute(new String[]{sb.toString()});
                    } else {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(this.mFilesPath);
                        sb2.append(str2);
                        sb2.append(NAMES[i3]);
                        sb2.append(str);
                        saveForground(sb2.toString());
                    }
                }
            } else if (i2 != -1) {
                SaveImageTask saveImageTask2 = new SaveImageTask();
                StringBuilder sb3 = new StringBuilder();
                sb3.append(this.mFilesPath);
                sb3.append(str2);
                sb3.append(NAMES[i3]);
                sb3.append(str);
                saveImageTask2.execute(new String[]{sb3.toString()});
            } else {
                StringBuilder sb4 = new StringBuilder();
                sb4.append(this.mFilesPath);
                sb4.append(str2);
                sb4.append(NAMES[i3]);
                sb4.append(str);
                saveForground(sb4.toString());
            }
            i2 = i3;
        }
        Toast.makeText(this, getResources().getString(C0905R.string.save_best_image_toast, new Object[]{Integer.valueOf(i)}), 0).show();
        backToViewfinder();
    }

    /* access modifiers changed from: private */
    public void backToViewfinder() {
        setResult(-1, new Intent());
        finish();
    }

    public void onResume() {
        super.onResume();
        if (this.mLoadingThread == null) {
            this.mLoadingThread = new ImageLoadingThread();
            this.mLoadingThread.start();
        }
    }

    public void onPause() {
        super.onPause();
    }

    /* access modifiers changed from: private */
    public void showProgressDialog() {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                BestpictureActivity bestpictureActivity = BestpictureActivity.this;
                bestpictureActivity.mProgressDialog = ProgressDialog.show(bestpictureActivity.mActivity, BuildConfig.FLAVOR, "Processing...", true, false);
                BestpictureActivity.this.mProgressDialog.show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void dismissProgressDialog() {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (BestpictureActivity.this.mProgressDialog != null && BestpictureActivity.this.mProgressDialog.isShowing() && !BestpictureActivity.this.mActivity.isFinishing()) {
                    BestpictureActivity.this.mProgressDialog.dismiss();
                    BestpictureActivity.this.mProgressDialog = null;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void saveForground(String str) {
        this.mNamedImages.nameNewImage(System.currentTimeMillis());
        NamedEntity nextNameEntity = this.mNamedImages.getNextNameEntity();
        if (nextNameEntity != null) {
            String str2 = nextNameEntity.title;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.mPlaceHolderUri.getPath());
            FileInputStream fileInputStream = new FileInputStream(str);
            byte[] bArr = new byte[4096];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read > 0) {
                    fileOutputStream.write(bArr, 0, read);
                } else {
                    fileInputStream.close();
                    fileOutputStream.close();
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }
}
