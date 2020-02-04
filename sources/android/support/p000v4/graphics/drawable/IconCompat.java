package android.support.p000v4.graphics.drawable;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.p002os.BuildCompat;
import android.support.p000v4.util.Preconditions;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import androidx.versionedparcelable.CustomVersionedParcelable;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

/* renamed from: android.support.v4.graphics.drawable.IconCompat */
public class IconCompat extends CustomVersionedParcelable {
    private static final float ADAPTIVE_ICON_INSET_FACTOR = 0.25f;
    private static final int AMBIENT_SHADOW_ALPHA = 30;
    private static final float BLUR_FACTOR = 0.010416667f;
    static final Mode DEFAULT_TINT_MODE = Mode.SRC_IN;
    private static final float DEFAULT_VIEW_PORT_SCALE = 0.6666667f;
    private static final String EXTRA_INT1 = "int1";
    private static final String EXTRA_INT2 = "int2";
    private static final String EXTRA_OBJ = "obj";
    private static final String EXTRA_TINT_LIST = "tint_list";
    private static final String EXTRA_TINT_MODE = "tint_mode";
    private static final String EXTRA_TYPE = "type";
    private static final float ICON_DIAMETER_FACTOR = 0.9166667f;
    private static final int KEY_SHADOW_ALPHA = 61;
    private static final float KEY_SHADOW_OFFSET_FACTOR = 0.020833334f;
    private static final String TAG = "IconCompat";
    public static final int TYPE_UNKNOWN = -1;
    @RestrictTo({Scope.LIBRARY})
    public byte[] mData;
    @RestrictTo({Scope.LIBRARY})
    public int mInt1;
    @RestrictTo({Scope.LIBRARY})
    public int mInt2;
    Object mObj1;
    @RestrictTo({Scope.LIBRARY})
    public Parcelable mParcelable;
    @RestrictTo({Scope.LIBRARY})
    public ColorStateList mTintList = null;
    Mode mTintMode = DEFAULT_TINT_MODE;
    @RestrictTo({Scope.LIBRARY})
    public String mTintModeStr;
    @RestrictTo({Scope.LIBRARY})
    public int mType;

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: android.support.v4.graphics.drawable.IconCompat$IconType */
    public @interface IconType {
    }

    private static String typeToString(int i) {
        return i != 1 ? i != 2 ? i != 3 ? i != 4 ? i != 5 ? "UNKNOWN" : "BITMAP_MASKABLE" : "URI" : "DATA" : "RESOURCE" : "BITMAP";
    }

    public static IconCompat createWithResource(Context context, @DrawableRes int i) {
        if (context != null) {
            IconCompat iconCompat = new IconCompat(2);
            iconCompat.mInt1 = i;
            iconCompat.mObj1 = context.getPackageName();
            return iconCompat;
        }
        throw new IllegalArgumentException("Context must not be null.");
    }

    @RestrictTo({Scope.LIBRARY})
    public static IconCompat createWithResource(String str, @DrawableRes int i) {
        if (str != null) {
            IconCompat iconCompat = new IconCompat(2);
            iconCompat.mInt1 = i;
            iconCompat.mObj1 = str;
            return iconCompat;
        }
        throw new IllegalArgumentException("Package must not be null.");
    }

    public static IconCompat createWithBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            IconCompat iconCompat = new IconCompat(1);
            iconCompat.mObj1 = bitmap;
            return iconCompat;
        }
        throw new IllegalArgumentException("Bitmap must not be null.");
    }

    public static IconCompat createWithAdaptiveBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            IconCompat iconCompat = new IconCompat(5);
            iconCompat.mObj1 = bitmap;
            return iconCompat;
        }
        throw new IllegalArgumentException("Bitmap must not be null.");
    }

    public static IconCompat createWithData(byte[] bArr, int i, int i2) {
        if (bArr != null) {
            IconCompat iconCompat = new IconCompat(3);
            iconCompat.mObj1 = bArr;
            iconCompat.mInt1 = i;
            iconCompat.mInt2 = i2;
            return iconCompat;
        }
        throw new IllegalArgumentException("Data must not be null.");
    }

    public static IconCompat createWithContentUri(String str) {
        if (str != null) {
            IconCompat iconCompat = new IconCompat(4);
            iconCompat.mObj1 = str;
            return iconCompat;
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    public static IconCompat createWithContentUri(Uri uri) {
        if (uri != null) {
            return createWithContentUri(uri.toString());
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    @RestrictTo({Scope.LIBRARY})
    public IconCompat() {
    }

    private IconCompat(int i) {
        this.mType = i;
    }

    public int getType() {
        if (this.mType != -1 || VERSION.SDK_INT < 23) {
            return this.mType;
        }
        return getType((Icon) this.mObj1);
    }

    @NonNull
    public String getResPackage() {
        if (this.mType == -1 && VERSION.SDK_INT >= 23) {
            return getResPackage((Icon) this.mObj1);
        }
        if (this.mType == 2) {
            return (String) this.mObj1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("called getResPackage() on ");
        sb.append(this);
        throw new IllegalStateException(sb.toString());
    }

    @IdRes
    public int getResId() {
        if (this.mType == -1 && VERSION.SDK_INT >= 23) {
            return getResId((Icon) this.mObj1);
        }
        if (this.mType == 2) {
            return this.mInt1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("called getResId() on ");
        sb.append(this);
        throw new IllegalStateException(sb.toString());
    }

    @NonNull
    public Uri getUri() {
        if (this.mType != -1 || VERSION.SDK_INT < 23) {
            return Uri.parse((String) this.mObj1);
        }
        return getUri((Icon) this.mObj1);
    }

    public IconCompat setTint(@ColorInt int i) {
        return setTintList(ColorStateList.valueOf(i));
    }

    public IconCompat setTintList(ColorStateList colorStateList) {
        this.mTintList = colorStateList;
        return this;
    }

    public IconCompat setTintMode(Mode mode) {
        this.mTintMode = mode;
        return this;
    }

    @RequiresApi(23)
    public Icon toIcon() {
        Icon icon;
        int i = this.mType;
        if (i == -1) {
            return (Icon) this.mObj1;
        }
        if (i == 1) {
            icon = Icon.createWithBitmap((Bitmap) this.mObj1);
        } else if (i == 2) {
            icon = Icon.createWithResource((String) this.mObj1, this.mInt1);
        } else if (i == 3) {
            icon = Icon.createWithData((byte[]) this.mObj1, this.mInt1, this.mInt2);
        } else if (i == 4) {
            icon = Icon.createWithContentUri((String) this.mObj1);
        } else if (i != 5) {
            throw new IllegalArgumentException("Unknown type");
        } else if (VERSION.SDK_INT >= 26) {
            icon = Icon.createWithAdaptiveBitmap((Bitmap) this.mObj1);
        } else {
            icon = Icon.createWithBitmap(createLegacyIconFromAdaptiveIcon((Bitmap) this.mObj1, false));
        }
        ColorStateList colorStateList = this.mTintList;
        if (colorStateList != null) {
            icon.setTintList(colorStateList);
        }
        Mode mode = this.mTintMode;
        if (mode != DEFAULT_TINT_MODE) {
            icon.setTintMode(mode);
        }
        return icon;
    }

    public Drawable loadDrawable(Context context) {
        if (VERSION.SDK_INT >= 23) {
            return toIcon().loadDrawable(context);
        }
        Drawable loadDrawableInner = loadDrawableInner(context);
        if (!(loadDrawableInner == null || (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE))) {
            loadDrawableInner.mutate();
            DrawableCompat.setTintList(loadDrawableInner, this.mTintList);
            DrawableCompat.setTintMode(loadDrawableInner, this.mTintMode);
        }
        return loadDrawableInner;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x008d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.drawable.Drawable loadDrawableInner(android.content.Context r9) {
        /*
            r8 = this;
            int r0 = r8.mType
            r1 = 1
            if (r0 == r1) goto L_0x0111
            r2 = 0
            r3 = 0
            r4 = 2
            java.lang.String r5 = "IconCompat"
            if (r0 == r4) goto L_0x00b1
            r1 = 3
            if (r0 == r1) goto L_0x009b
            r1 = 4
            if (r0 == r1) goto L_0x0029
            r1 = 5
            if (r0 == r1) goto L_0x0017
            goto L_0x0110
        L_0x0017:
            android.graphics.drawable.BitmapDrawable r0 = new android.graphics.drawable.BitmapDrawable
            android.content.res.Resources r9 = r9.getResources()
            java.lang.Object r8 = r8.mObj1
            android.graphics.Bitmap r8 = (android.graphics.Bitmap) r8
            android.graphics.Bitmap r8 = createLegacyIconFromAdaptiveIcon(r8, r3)
            r0.<init>(r9, r8)
            return r0
        L_0x0029:
            java.lang.Object r0 = r8.mObj1
            java.lang.String r0 = (java.lang.String) r0
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = r0.getScheme()
            java.lang.String r3 = "content"
            boolean r3 = r3.equals(r1)
            if (r3 != 0) goto L_0x006c
            java.lang.String r3 = "file"
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0046
            goto L_0x006c
        L_0x0046:
            java.io.FileInputStream r1 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x0056 }
            java.io.File r3 = new java.io.File     // Catch:{ FileNotFoundException -> 0x0056 }
            java.lang.Object r8 = r8.mObj1     // Catch:{ FileNotFoundException -> 0x0056 }
            java.lang.String r8 = (java.lang.String) r8     // Catch:{ FileNotFoundException -> 0x0056 }
            r3.<init>(r8)     // Catch:{ FileNotFoundException -> 0x0056 }
            r1.<init>(r3)     // Catch:{ FileNotFoundException -> 0x0056 }
            r8 = r1
            goto L_0x008b
        L_0x0056:
            r8 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "Unable to load image from path: "
            r1.append(r3)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.util.Log.w(r5, r0, r8)
            goto L_0x008a
        L_0x006c:
            android.content.ContentResolver r8 = r9.getContentResolver()     // Catch:{ Exception -> 0x0075 }
            java.io.InputStream r8 = r8.openInputStream(r0)     // Catch:{ Exception -> 0x0075 }
            goto L_0x008b
        L_0x0075:
            r8 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "Unable to load image from URI: "
            r1.append(r3)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.util.Log.w(r5, r0, r8)
        L_0x008a:
            r8 = r2
        L_0x008b:
            if (r8 == 0) goto L_0x0110
            android.graphics.drawable.BitmapDrawable r0 = new android.graphics.drawable.BitmapDrawable
            android.content.res.Resources r9 = r9.getResources()
            android.graphics.Bitmap r8 = android.graphics.BitmapFactory.decodeStream(r8)
            r0.<init>(r9, r8)
            return r0
        L_0x009b:
            android.graphics.drawable.BitmapDrawable r0 = new android.graphics.drawable.BitmapDrawable
            android.content.res.Resources r9 = r9.getResources()
            java.lang.Object r1 = r8.mObj1
            byte[] r1 = (byte[]) r1
            int r2 = r8.mInt1
            int r8 = r8.mInt2
            android.graphics.Bitmap r8 = android.graphics.BitmapFactory.decodeByteArray(r1, r2, r8)
            r0.<init>(r9, r8)
            return r0
        L_0x00b1:
            java.lang.Object r0 = r8.mObj1
            java.lang.String r0 = (java.lang.String) r0
            boolean r6 = android.text.TextUtils.isEmpty(r0)
            if (r6 == 0) goto L_0x00bf
            java.lang.String r0 = r9.getPackageName()
        L_0x00bf:
            java.lang.String r6 = "android"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x00cc
            android.content.res.Resources r0 = android.content.res.Resources.getSystem()
            goto L_0x00dc
        L_0x00cc:
            android.content.pm.PackageManager r6 = r9.getPackageManager()
            r7 = 8192(0x2000, float:1.14794E-41)
            android.content.pm.ApplicationInfo r7 = r6.getApplicationInfo(r0, r7)     // Catch:{ NameNotFoundException -> 0x0100 }
            if (r7 == 0) goto L_0x0110
            android.content.res.Resources r0 = r6.getResourcesForApplication(r7)     // Catch:{ NameNotFoundException -> 0x0100 }
        L_0x00dc:
            int r6 = r8.mInt1     // Catch:{ RuntimeException -> 0x00e7 }
            android.content.res.Resources$Theme r9 = r9.getTheme()     // Catch:{ RuntimeException -> 0x00e7 }
            android.graphics.drawable.Drawable r8 = android.support.p000v4.content.res.ResourcesCompat.getDrawable(r0, r6, r9)     // Catch:{ RuntimeException -> 0x00e7 }
            return r8
        L_0x00e7:
            r9 = move-exception
            java.lang.Object[] r0 = new java.lang.Object[r4]
            int r4 = r8.mInt1
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r0[r3] = r4
            java.lang.Object r8 = r8.mObj1
            r0[r1] = r8
            java.lang.String r8 = "Unable to load resource 0x%08x from pkg=%s"
            java.lang.String r8 = java.lang.String.format(r8, r0)
            android.util.Log.e(r5, r8, r9)
            goto L_0x0110
        L_0x0100:
            r9 = move-exception
            java.lang.Object[] r4 = new java.lang.Object[r4]
            r4[r3] = r0
            r4[r1] = r8
            java.lang.String r8 = "Unable to find pkg=%s for icon %s"
            java.lang.String r8 = java.lang.String.format(r8, r4)
            android.util.Log.e(r5, r8, r9)
        L_0x0110:
            return r2
        L_0x0111:
            android.graphics.drawable.BitmapDrawable r0 = new android.graphics.drawable.BitmapDrawable
            android.content.res.Resources r9 = r9.getResources()
            java.lang.Object r8 = r8.mObj1
            android.graphics.Bitmap r8 = (android.graphics.Bitmap) r8
            r0.<init>(r9, r8)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.graphics.drawable.IconCompat.loadDrawableInner(android.content.Context):android.graphics.drawable.Drawable");
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void addToShortcutIntent(@NonNull Intent intent, @Nullable Drawable drawable, @NonNull Context context) {
        Bitmap bitmap;
        Bitmap bitmap2;
        int i = this.mType;
        if (i == 1) {
            bitmap = (Bitmap) this.mObj1;
            if (drawable != null) {
                bitmap = bitmap.copy(bitmap.getConfig(), true);
            }
        } else if (i == 2) {
            try {
                Context createPackageContext = context.createPackageContext((String) this.mObj1, 0);
                if (drawable == null) {
                    intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(createPackageContext, this.mInt1));
                    return;
                }
                Drawable drawable2 = ContextCompat.getDrawable(createPackageContext, this.mInt1);
                if (drawable2.getIntrinsicWidth() > 0) {
                    if (drawable2.getIntrinsicHeight() > 0) {
                        bitmap2 = Bitmap.createBitmap(drawable2.getIntrinsicWidth(), drawable2.getIntrinsicHeight(), Config.ARGB_8888);
                        drawable2.setBounds(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
                        drawable2.draw(new Canvas(bitmap2));
                        bitmap = bitmap2;
                    }
                }
                int launcherLargeIconSize = ((ActivityManager) createPackageContext.getSystemService("activity")).getLauncherLargeIconSize();
                bitmap2 = Bitmap.createBitmap(launcherLargeIconSize, launcherLargeIconSize, Config.ARGB_8888);
                drawable2.setBounds(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
                drawable2.draw(new Canvas(bitmap2));
                bitmap = bitmap2;
            } catch (NameNotFoundException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Can't find package ");
                sb.append(this.mObj1);
                throw new IllegalArgumentException(sb.toString(), e);
            }
        } else if (i == 5) {
            bitmap = createLegacyIconFromAdaptiveIcon((Bitmap) this.mObj1, true);
        } else {
            throw new IllegalArgumentException("Icon type not supported for intent shortcuts");
        }
        if (drawable != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            drawable.setBounds(width / 2, height / 2, width, height);
            drawable.draw(new Canvas(bitmap));
        }
        intent.putExtra("android.intent.extra.shortcut.ICON", bitmap);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        int i = this.mType;
        String str = EXTRA_OBJ;
        if (i != -1) {
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        bundle.putByteArray(str, (byte[]) this.mObj1);
                    } else if (i != 4) {
                        if (i != 5) {
                            throw new IllegalArgumentException("Invalid icon");
                        }
                    }
                }
                bundle.putString(str, (String) this.mObj1);
            }
            bundle.putParcelable(str, (Bitmap) this.mObj1);
        } else {
            bundle.putParcelable(str, (Parcelable) this.mObj1);
        }
        bundle.putInt(EXTRA_TYPE, this.mType);
        bundle.putInt(EXTRA_INT1, this.mInt1);
        bundle.putInt(EXTRA_INT2, this.mInt2);
        ColorStateList colorStateList = this.mTintList;
        if (colorStateList != null) {
            bundle.putParcelable(EXTRA_TINT_LIST, colorStateList);
        }
        Mode mode = this.mTintMode;
        if (mode != DEFAULT_TINT_MODE) {
            bundle.putString(EXTRA_TINT_MODE, mode.name());
        }
        return bundle;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
        if (r1 != 5) goto L_0x009a;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ae  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String toString() {
        /*
            r4 = this;
            int r0 = r4.mType
            r1 = -1
            if (r0 != r1) goto L_0x000c
            java.lang.Object r4 = r4.mObj1
            java.lang.String r4 = java.lang.String.valueOf(r4)
            return r4
        L_0x000c:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "Icon(typ="
            r0.<init>(r1)
            int r1 = r4.mType
            java.lang.String r1 = typeToString(r1)
            r0.append(r1)
            int r1 = r4.mType
            r2 = 1
            if (r1 == r2) goto L_0x007a
            r3 = 2
            if (r1 == r3) goto L_0x0052
            r2 = 3
            if (r1 == r2) goto L_0x0039
            r2 = 4
            if (r1 == r2) goto L_0x002e
            r2 = 5
            if (r1 == r2) goto L_0x007a
            goto L_0x009a
        L_0x002e:
            java.lang.String r1 = " uri="
            r0.append(r1)
            java.lang.Object r1 = r4.mObj1
            r0.append(r1)
            goto L_0x009a
        L_0x0039:
            java.lang.String r1 = " len="
            r0.append(r1)
            int r1 = r4.mInt1
            r0.append(r1)
            int r1 = r4.mInt2
            if (r1 == 0) goto L_0x009a
            java.lang.String r1 = " off="
            r0.append(r1)
            int r1 = r4.mInt2
            r0.append(r1)
            goto L_0x009a
        L_0x0052:
            java.lang.String r1 = " pkg="
            r0.append(r1)
            java.lang.String r1 = r4.getResPackage()
            r0.append(r1)
            java.lang.String r1 = " id="
            r0.append(r1)
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r2 = 0
            int r3 = r4.getResId()
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            java.lang.String r2 = "0x%08x"
            java.lang.String r1 = java.lang.String.format(r2, r1)
            r0.append(r1)
            goto L_0x009a
        L_0x007a:
            java.lang.String r1 = " size="
            r0.append(r1)
            java.lang.Object r1 = r4.mObj1
            android.graphics.Bitmap r1 = (android.graphics.Bitmap) r1
            int r1 = r1.getWidth()
            r0.append(r1)
            java.lang.String r1 = "x"
            r0.append(r1)
            java.lang.Object r1 = r4.mObj1
            android.graphics.Bitmap r1 = (android.graphics.Bitmap) r1
            int r1 = r1.getHeight()
            r0.append(r1)
        L_0x009a:
            android.content.res.ColorStateList r1 = r4.mTintList
            if (r1 == 0) goto L_0x00a8
            java.lang.String r1 = " tint="
            r0.append(r1)
            android.content.res.ColorStateList r1 = r4.mTintList
            r0.append(r1)
        L_0x00a8:
            android.graphics.PorterDuff$Mode r1 = r4.mTintMode
            android.graphics.PorterDuff$Mode r2 = DEFAULT_TINT_MODE
            if (r1 == r2) goto L_0x00b8
            java.lang.String r1 = " mode="
            r0.append(r1)
            android.graphics.PorterDuff$Mode r4 = r4.mTintMode
            r0.append(r4)
        L_0x00b8:
            java.lang.String r4 = ")"
            r0.append(r4)
            java.lang.String r4 = r0.toString()
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.graphics.drawable.IconCompat.toString():java.lang.String");
    }

    public void onPreParceling(boolean z) {
        this.mTintModeStr = this.mTintMode.name();
        int i = this.mType;
        if (i != -1) {
            if (i != 1) {
                String str = "UTF-16";
                if (i == 2) {
                    this.mData = ((String) this.mObj1).getBytes(Charset.forName(str));
                    return;
                } else if (i == 3) {
                    this.mData = (byte[]) this.mObj1;
                    return;
                } else if (i == 4) {
                    this.mData = this.mObj1.toString().getBytes(Charset.forName(str));
                    return;
                } else if (i != 5) {
                    return;
                }
            }
            if (z) {
                Bitmap bitmap = (Bitmap) this.mObj1;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 90, byteArrayOutputStream);
                this.mData = byteArrayOutputStream.toByteArray();
                return;
            }
            this.mParcelable = (Parcelable) this.mObj1;
        } else if (!z) {
            this.mParcelable = (Parcelable) this.mObj1;
        } else {
            throw new IllegalArgumentException("Can't serialize Icon created with IconCompat#createFromIcon");
        }
    }

    public void onPostParceling() {
        this.mTintMode = Mode.valueOf(this.mTintModeStr);
        int i = this.mType;
        if (i != -1) {
            if (i != 1) {
                String str = "UTF-16";
                if (i == 2) {
                    this.mObj1 = new String(this.mData, Charset.forName(str));
                    return;
                } else if (i == 3) {
                    this.mObj1 = this.mData;
                    return;
                } else if (i == 4) {
                    this.mObj1 = Uri.parse(new String(this.mData, Charset.forName(str)));
                    return;
                } else if (i != 5) {
                    return;
                }
            }
            Parcelable parcelable = this.mParcelable;
            if (parcelable != null) {
                this.mObj1 = parcelable;
                return;
            }
            byte[] bArr = this.mData;
            this.mObj1 = bArr;
            this.mType = 3;
            this.mInt1 = 0;
            this.mInt2 = bArr.length;
            return;
        }
        Parcelable parcelable2 = this.mParcelable;
        if (parcelable2 != null) {
            this.mObj1 = parcelable2;
            return;
        }
        throw new IllegalArgumentException("Invalid icon");
    }

    @Nullable
    public static IconCompat createFromBundle(@NonNull Bundle bundle) {
        int i = bundle.getInt(EXTRA_TYPE);
        IconCompat iconCompat = new IconCompat(i);
        iconCompat.mInt1 = bundle.getInt(EXTRA_INT1);
        iconCompat.mInt2 = bundle.getInt(EXTRA_INT2);
        String str = EXTRA_TINT_LIST;
        if (bundle.containsKey(str)) {
            iconCompat.mTintList = (ColorStateList) bundle.getParcelable(str);
        }
        String str2 = EXTRA_TINT_MODE;
        if (bundle.containsKey(str2)) {
            iconCompat.mTintMode = Mode.valueOf(bundle.getString(str2));
        }
        String str3 = EXTRA_OBJ;
        if (!(i == -1 || i == 1)) {
            if (i != 2) {
                if (i == 3) {
                    iconCompat.mObj1 = bundle.getByteArray(str3);
                    return iconCompat;
                } else if (i != 4) {
                    if (i != 5) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Unknown type ");
                        sb.append(i);
                        Log.w(TAG, sb.toString());
                        return null;
                    }
                }
            }
            iconCompat.mObj1 = bundle.getString(str3);
            return iconCompat;
        }
        iconCompat.mObj1 = bundle.getParcelable(str3);
        return iconCompat;
    }

    @Nullable
    @RequiresApi(23)
    public static IconCompat createFromIcon(@NonNull Icon icon) {
        Preconditions.checkNotNull(icon);
        int type = getType(icon);
        if (type == 2) {
            return createWithResource(getResPackage(icon), getResId(icon));
        }
        if (type == 4) {
            return createWithContentUri(getUri(icon));
        }
        IconCompat iconCompat = new IconCompat(-1);
        iconCompat.mObj1 = icon;
        return iconCompat;
    }

    @RequiresApi(23)
    public static int getType(@NonNull Icon icon) {
        String str = "Unable to get icon type ";
        String str2 = TAG;
        if (BuildCompat.isAtLeastP()) {
            return icon.getType();
        }
        try {
            return ((Integer) icon.getClass().getMethod("getType", new Class[0]).invoke(icon, new Object[0])).intValue();
        } catch (IllegalAccessException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(icon);
            Log.e(str2, sb.toString(), e);
            return -1;
        } catch (InvocationTargetException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(icon);
            Log.e(str2, sb2.toString(), e2);
            return -1;
        } catch (NoSuchMethodException e3) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str);
            sb3.append(icon);
            Log.e(str2, sb3.toString(), e3);
            return -1;
        }
    }

    @Nullable
    @RequiresApi(23)
    public static String getResPackage(@NonNull Icon icon) {
        String str = "Unable to get icon package";
        String str2 = TAG;
        if (BuildCompat.isAtLeastP()) {
            return icon.getResPackage();
        }
        try {
            return (String) icon.getClass().getMethod("getResPackage", new Class[0]).invoke(icon, new Object[0]);
        } catch (IllegalAccessException e) {
            Log.e(str2, str, e);
            return null;
        } catch (InvocationTargetException e2) {
            Log.e(str2, str, e2);
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(str2, str, e3);
            return null;
        }
    }

    @RequiresApi(23)
    @DrawableRes
    @IdRes
    public static int getResId(@NonNull Icon icon) {
        String str = "Unable to get icon resource";
        String str2 = TAG;
        if (BuildCompat.isAtLeastP()) {
            return icon.getResId();
        }
        try {
            return ((Integer) icon.getClass().getMethod("getResId", new Class[0]).invoke(icon, new Object[0])).intValue();
        } catch (IllegalAccessException e) {
            Log.e(str2, str, e);
            return 0;
        } catch (InvocationTargetException e2) {
            Log.e(str2, str, e2);
            return 0;
        } catch (NoSuchMethodException e3) {
            Log.e(str2, str, e3);
            return 0;
        }
    }

    @Nullable
    @RequiresApi(23)
    public static Uri getUri(@NonNull Icon icon) {
        String str = "Unable to get icon uri";
        String str2 = TAG;
        if (BuildCompat.isAtLeastP()) {
            return icon.getUri();
        }
        try {
            return (Uri) icon.getClass().getMethod("getUri", new Class[0]).invoke(icon, new Object[0]);
        } catch (IllegalAccessException e) {
            Log.e(str2, str, e);
            return null;
        } catch (InvocationTargetException e2) {
            Log.e(str2, str, e2);
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(str2, str, e3);
            return null;
        }
    }

    @VisibleForTesting
    static Bitmap createLegacyIconFromAdaptiveIcon(Bitmap bitmap, boolean z) {
        int min = (int) (((float) Math.min(bitmap.getWidth(), bitmap.getHeight())) * DEFAULT_VIEW_PORT_SCALE);
        Bitmap createBitmap = Bitmap.createBitmap(min, min, Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(3);
        float f = (float) min;
        float f2 = 0.5f * f;
        float f3 = ICON_DIAMETER_FACTOR * f2;
        if (z) {
            float f4 = BLUR_FACTOR * f;
            paint.setColor(0);
            paint.setShadowLayer(f4, 0.0f, f * KEY_SHADOW_OFFSET_FACTOR, 1023410176);
            canvas.drawCircle(f2, f2, f3, paint);
            paint.setShadowLayer(f4, 0.0f, 0.0f, 503316480);
            canvas.drawCircle(f2, f2, f3, paint);
            paint.clearShadowLayer();
        }
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        TileMode tileMode = TileMode.CLAMP;
        BitmapShader bitmapShader = new BitmapShader(bitmap, tileMode, tileMode);
        Matrix matrix = new Matrix();
        matrix.setTranslate((float) ((-(bitmap.getWidth() - min)) / 2), (float) ((-(bitmap.getHeight() - min)) / 2));
        bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        canvas.drawCircle(f2, f2, f3, paint);
        canvas.setBitmap(null);
        return createBitmap;
    }
}
