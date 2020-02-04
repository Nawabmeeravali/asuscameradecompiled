package com.android.camera.p004ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.camera.data.MediaDetails;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.DetailsDialog */
public class DetailsDialog {

    /* renamed from: com.android.camera.ui.DetailsDialog$DetailsAdapter */
    private static class DetailsAdapter extends BaseAdapter {
        private final Context mContext;
        private final DecimalFormat mDecimalFormat = new DecimalFormat(".####");
        private final Locale mDefaultLocale = Locale.getDefault();
        private int mHeightIndex = -1;
        private final ArrayList<String> mItems;
        private final MediaDetails mMediaDetails;
        private int mWidthIndex = -1;

        public boolean areAllItemsEnabled() {
            return false;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean isEnabled(int i) {
            return false;
        }

        public DetailsAdapter(Context context, MediaDetails mediaDetails) {
            this.mContext = context;
            this.mMediaDetails = mediaDetails;
            this.mItems = new ArrayList<>(mediaDetails.size());
            setDetails(context, mediaDetails);
        }

        /* JADX WARNING: Removed duplicated region for block: B:54:0x01dc  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x01f9  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void setDetails(android.content.Context r22, com.android.camera.data.MediaDetails r23) {
            /*
                r21 = this;
                r0 = r21
                r1 = r22
                r2 = r23
                java.util.Iterator r3 = r23.iterator()
                r4 = 0
                r5 = 1
                java.lang.Integer r6 = java.lang.Integer.valueOf(r5)
                r8 = r4
                r7 = r5
            L_0x0012:
                boolean r9 = r3.hasNext()
                if (r9 == 0) goto L_0x0213
                java.lang.Object r9 = r3.next()
                java.util.Map$Entry r9 = (java.util.Map.Entry) r9
                java.lang.Object r10 = r9.getKey()
                java.lang.Integer r10 = (java.lang.Integer) r10
                int r10 = r10.intValue()
                r11 = 5
                r12 = 2131690775(0x7f0f0517, float:1.9010603E38)
                java.lang.String r13 = "0"
                r14 = 2
                r15 = 0
                if (r10 == r11) goto L_0x01a8
                r11 = 6
                if (r10 == r11) goto L_0x0184
                r11 = 7
                if (r10 == r11) goto L_0x017b
                r11 = 10
                if (r10 == r11) goto L_0x016c
                r11 = 200(0xc8, float:2.8E-43)
                if (r10 == r11) goto L_0x0149
                r11 = 107(0x6b, float:1.5E-43)
                if (r10 == r11) goto L_0x00cd
                r11 = 108(0x6c, float:1.51E-43)
                if (r10 == r11) goto L_0x00bd
                switch(r10) {
                    case 102: goto L_0x009f;
                    case 103: goto L_0x008d;
                    case 104: goto L_0x006f;
                    default: goto L_0x004b;
                }
            L_0x004b:
                java.lang.Object r10 = r9.getValue()
                if (r10 == 0) goto L_0x0057
                java.lang.String r10 = r10.toString()
                goto L_0x01cc
            L_0x0057:
                java.lang.Object[] r0 = new java.lang.Object[r5]
                java.lang.Object r2 = r9.getKey()
                java.lang.Integer r2 = (java.lang.Integer) r2
                int r2 = r2.intValue()
                java.lang.String r1 = com.android.camera.p004ui.DetailsDialog.getDetailsName(r1, r2)
                r0[r15] = r1
                java.lang.String r1 = "%s's value is Null"
                com.android.camera.p004ui.DetailsDialog.access$000(r1, r0)
                throw r4
            L_0x006f:
                java.lang.Object r10 = r9.getValue()
                java.lang.String r11 = "1"
                boolean r10 = r11.equals(r10)
                if (r10 == 0) goto L_0x0084
                r10 = 2131689676(0x7f0f00cc, float:1.9008374E38)
                java.lang.String r10 = r1.getString(r10)
                goto L_0x01cc
            L_0x0084:
                r10 = 2131689505(0x7f0f0021, float:1.9008027E38)
                java.lang.String r10 = r1.getString(r10)
                goto L_0x01cc
            L_0x008d:
                java.lang.Object r10 = r9.getValue()
                java.lang.String r10 = r10.toString()
                double r10 = java.lang.Double.parseDouble(r10)
                java.lang.String r10 = r0.toLocalNumber(r10)
                goto L_0x01cc
            L_0x009f:
                java.lang.Object r10 = r9.getValue()
                com.android.camera.data.MediaDetails$FlashState r10 = (com.android.camera.data.MediaDetails.FlashState) r10
                boolean r10 = r10.isFlashFired()
                if (r10 == 0) goto L_0x00b4
                r10 = 2131689614(0x7f0f008e, float:1.9008248E38)
                java.lang.String r10 = r1.getString(r10)
                goto L_0x01cc
            L_0x00b4:
                r10 = 2131689613(0x7f0f008d, float:1.9008246E38)
                java.lang.String r10 = r1.getString(r10)
                goto L_0x01cc
            L_0x00bd:
                java.lang.Object r10 = r9.getValue()
                java.lang.String r10 = (java.lang.String) r10
                int r10 = java.lang.Integer.parseInt(r10)
                java.lang.String r10 = r0.toLocalNumber(r10)
                goto L_0x01cc
            L_0x00cd:
                java.lang.Object r10 = r9.getValue()
                java.lang.String r10 = (java.lang.String) r10
                java.lang.Double r10 = java.lang.Double.valueOf(r10)
                double r10 = r10.doubleValue()
                r12 = 4607182418800017408(0x3ff0000000000000, double:1.0)
                int r16 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                r17 = 4602678819172646912(0x3fe0000000000000, double:0.5)
                if (r16 >= 0) goto L_0x00fc
                java.util.Locale r4 = r0.mDefaultLocale
                java.lang.Object[] r5 = new java.lang.Object[r14]
                r5[r15] = r6
                double r12 = r12 / r10
                double r12 = r12 + r17
                int r10 = (int) r12
                java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
                r11 = 1
                r5[r11] = r10
                java.lang.String r10 = "%d/%d"
                java.lang.String r10 = java.lang.String.format(r4, r10, r5)
                goto L_0x01cc
            L_0x00fc:
                int r4 = (int) r10
                double r12 = (double) r4
                double r10 = r10 - r12
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r4 = java.lang.String.valueOf(r4)
                r5.append(r4)
                java.lang.String r4 = "''"
                r5.append(r4)
                java.lang.String r4 = r5.toString()
                r12 = 4547007122018943789(0x3f1a36e2eb1c432d, double:1.0E-4)
                int r5 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r5 <= 0) goto L_0x0146
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                r5.append(r4)
                java.util.Locale r4 = r0.mDefaultLocale
                java.lang.Object[] r12 = new java.lang.Object[r14]
                r12[r15] = r6
                r19 = 4607182418800017408(0x3ff0000000000000, double:1.0)
                double r10 = r19 / r10
                double r10 = r10 + r17
                int r10 = (int) r10
                java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
                r11 = 1
                r12[r11] = r10
                java.lang.String r10 = " %d/%d"
                java.lang.String r4 = java.lang.String.format(r4, r10, r12)
                r5.append(r4)
                java.lang.String r4 = r5.toString()
            L_0x0146:
                r10 = r4
                goto L_0x01cc
            L_0x0149:
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r5 = "\n"
                r4.append(r5)
                java.lang.Object r5 = r9.getValue()
                java.lang.String r5 = r5.toString()
                r4.append(r5)
                java.lang.String r10 = r4.toString()
                java.lang.Object r4 = r9.getValue()
                java.lang.String r4 = r4.toString()
                r8 = r4
                goto L_0x01cc
            L_0x016c:
                java.lang.Object r4 = r9.getValue()
                java.lang.Long r4 = (java.lang.Long) r4
                long r4 = r4.longValue()
                java.lang.String r10 = android.text.format.Formatter.formatFileSize(r1, r4)
                goto L_0x01cc
            L_0x017b:
                java.lang.Object r4 = r9.getValue()
                java.lang.String r10 = r0.toLocalInteger(r4)
                goto L_0x01cc
            L_0x0184:
                java.util.ArrayList<java.lang.String> r4 = r0.mItems
                int r4 = r4.size()
                r0.mHeightIndex = r4
                java.lang.Object r4 = r9.getValue()
                java.lang.String r4 = r4.toString()
                boolean r4 = r4.equalsIgnoreCase(r13)
                if (r4 == 0) goto L_0x019f
                java.lang.String r10 = r1.getString(r12)
                goto L_0x01c2
            L_0x019f:
                java.lang.Object r4 = r9.getValue()
                java.lang.String r10 = r0.toLocalInteger(r4)
                goto L_0x01cc
            L_0x01a8:
                java.util.ArrayList<java.lang.String> r4 = r0.mItems
                int r4 = r4.size()
                r0.mWidthIndex = r4
                java.lang.Object r4 = r9.getValue()
                java.lang.String r4 = r4.toString()
                boolean r4 = r4.equalsIgnoreCase(r13)
                if (r4 == 0) goto L_0x01c4
                java.lang.String r10 = r1.getString(r12)
            L_0x01c2:
                r7 = r15
                goto L_0x01cc
            L_0x01c4:
                java.lang.Object r4 = r9.getValue()
                java.lang.String r10 = r0.toLocalInteger(r4)
            L_0x01cc:
                java.lang.Object r4 = r9.getKey()
                java.lang.Integer r4 = (java.lang.Integer) r4
                int r4 = r4.intValue()
                boolean r5 = r2.hasUnit(r4)
                if (r5 == 0) goto L_0x01f9
                r5 = 3
                java.lang.Object[] r5 = new java.lang.Object[r5]
                java.lang.String r9 = com.android.camera.p004ui.DetailsDialog.getDetailsName(r1, r4)
                r5[r15] = r9
                r9 = 1
                r5[r9] = r10
                int r4 = r2.getUnit(r4)
                java.lang.String r4 = r1.getString(r4)
                r5[r14] = r4
                java.lang.String r4 = "%s: %s %s"
                java.lang.String r4 = java.lang.String.format(r4, r5)
                goto L_0x020a
            L_0x01f9:
                r9 = 1
                java.lang.Object[] r5 = new java.lang.Object[r14]
                java.lang.String r4 = com.android.camera.p004ui.DetailsDialog.getDetailsName(r1, r4)
                r5[r15] = r4
                r5[r9] = r10
                java.lang.String r4 = "%s: %s"
                java.lang.String r4 = java.lang.String.format(r4, r5)
            L_0x020a:
                java.util.ArrayList<java.lang.String> r5 = r0.mItems
                r5.add(r4)
                r5 = r9
                r4 = 0
                goto L_0x0012
            L_0x0213:
                if (r7 != 0) goto L_0x0218
                r0.resolveResolution(r8)
            L_0x0218:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.camera.p004ui.DetailsDialog.DetailsAdapter.setDetails(android.content.Context, com.android.camera.data.MediaDetails):void");
        }

        public void resolveResolution(String str) {
            Bitmap decodeFile = BitmapFactory.decodeFile(str);
            if (decodeFile != null) {
                onResolutionAvailable(decodeFile.getWidth(), decodeFile.getHeight());
            }
        }

        public int getCount() {
            return this.mItems.size();
        }

        public Object getItem(int i) {
            return this.mMediaDetails.getDetail(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView;
            if (view == null) {
                textView = (TextView) LayoutInflater.from(this.mContext).inflate(C0905R.layout.details, viewGroup, false);
            } else {
                textView = (TextView) view;
            }
            textView.setText((CharSequence) this.mItems.get(i));
            return textView;
        }

        public void onResolutionAvailable(int i, int i2) {
            if (i != 0 && i2 != 0) {
                Object[] objArr = {DetailsDialog.getDetailsName(this.mContext, 5), Integer.valueOf(i)};
                String str = "%s: %d";
                String format = String.format(this.mDefaultLocale, str, objArr);
                String format2 = String.format(this.mDefaultLocale, str, new Object[]{DetailsDialog.getDetailsName(this.mContext, 6), Integer.valueOf(i2)});
                this.mItems.set(this.mWidthIndex, String.valueOf(format));
                this.mItems.set(this.mHeightIndex, String.valueOf(format2));
                notifyDataSetChanged();
            }
        }

        private String toLocalInteger(Object obj) {
            if (obj instanceof Integer) {
                return toLocalNumber(((Integer) obj).intValue());
            }
            String obj2 = obj.toString();
            try {
                obj2 = toLocalNumber(Integer.parseInt(obj2));
            } catch (NumberFormatException unused) {
            }
            return obj2;
        }

        private String toLocalNumber(int i) {
            return String.format(this.mDefaultLocale, "%d", new Object[]{Integer.valueOf(i)});
        }

        private String toLocalNumber(double d) {
            return this.mDecimalFormat.format(d);
        }
    }

    static /* synthetic */ void access$000(String str, Object[] objArr) {
        fail(str, objArr);
        throw null;
    }

    public static Dialog create(Context context, MediaDetails mediaDetails) {
        ListView listView = (ListView) LayoutInflater.from(context).inflate(C0905R.layout.details_list, null, false);
        listView.setAdapter(new DetailsAdapter(context, mediaDetails));
        return new Builder(context).setTitle(C0905R.string.details).setView(listView).setPositiveButton(C0905R.string.close, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create();
    }

    public static String getDetailsName(Context context, int i) {
        if (i == 107) {
            return context.getString(C0905R.string.exposure_time);
        }
        if (i == 108) {
            return context.getString(C0905R.string.iso);
        }
        if (i == 200) {
            return context.getString(C0905R.string.path);
        }
        switch (i) {
            case 1:
                return context.getString(C0905R.string.title);
            case 2:
                return context.getString(C0905R.string.description);
            case 3:
                return context.getString(C0905R.string.time);
            case 4:
                return context.getString(C0905R.string.location);
            case 5:
                return context.getString(C0905R.string.width);
            case 6:
                return context.getString(C0905R.string.height);
            case 7:
                return context.getString(C0905R.string.orientation);
            case 8:
                return context.getString(C0905R.string.duration);
            case 9:
                return context.getString(C0905R.string.mimetype);
            case 10:
                return context.getString(C0905R.string.file_size);
            default:
                switch (i) {
                    case 100:
                        return context.getString(C0905R.string.maker);
                    case 101:
                        return context.getString(C0905R.string.model);
                    case 102:
                        return context.getString(C0905R.string.flash);
                    case 103:
                        return context.getString(C0905R.string.focal_length);
                    case 104:
                        return context.getString(C0905R.string.white_balance);
                    case 105:
                        return context.getString(C0905R.string.aperture);
                    default:
                        StringBuilder sb = new StringBuilder();
                        sb.append("Unknown key");
                        sb.append(i);
                        return sb.toString();
                }
        }
    }

    private static void fail(String str, Object... objArr) {
        if (objArr.length != 0) {
            str = String.format(str, objArr);
        }
        throw new AssertionError(str);
    }
}
