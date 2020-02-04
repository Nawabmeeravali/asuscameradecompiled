package com.android.camera.p004ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import java.lang.reflect.Array;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.MenuHelp */
public class MenuHelp extends RotatableLayout {
    private static final int HEIGHT_GRID = 7;
    private static final int HELP_0_0_INDEX = 0;
    private static final int HELP_1_0_INDEX = 1;
    private static final int HELP_3_0_INDEX = 2;
    private static final int HELP_4_6_INDEX = 3;
    private static final int LAYOUT_ARROW_HEIGHT_DEVIATION = 35;
    private static final int LAYOUT_ARROW_WIDTH_DEVIATION = 20;
    private static final int MAX_INDEX = 5;
    private static final int OK_2_4_INDEX = 4;
    private static final int POINT_MARGIN = 50;
    private static final String TAG = "MenuHelp";
    private static final int WIDTH_GRID = 5;
    private static int mBottomMargin;
    private static int mTopMargin;
    private boolean forCamera2;
    private Arrows mArrows;
    private View mBackgroundView;
    private Context mContext;
    private float mCurrentWidth;
    private RotateLayout mHelp0_0;
    private float mHelp1Location;
    private RotateLayout mHelp1_0;
    private RotateLayout mHelp3_0;
    private RotateLayout mHelp4_6;
    private float[][] mLocX;
    private float[][] mLocY;
    private RotateLayout mOk2_4;
    private int mOrientation;
    private Typeface mTypeface;

    public MenuHelp(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLocX = (float[][]) Array.newInstance(float.class, new int[]{4, 5});
        this.mLocY = (float[][]) Array.newInstance(float.class, new int[]{4, 5});
        this.forCamera2 = false;
        this.mCurrentWidth = 0.0f;
        this.mHelp1Location = 80.0f;
        this.mContext = context;
        this.mTypeface = Typeface.create(Typeface.SERIF, 0);
    }

    public MenuHelp(Context context) {
        this(context, null);
    }

    public void setForCamera2(boolean z) {
        this.forCamera2 = z;
    }

    public void setMargins(int i, int i2) {
        mTopMargin = i;
        mBottomMargin = i2;
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        for (int i7 = 0; i7 < getChildCount(); i7++) {
            getChildAt(i7).layout(0, 0, i5, i6);
        }
        setLocation(i5 - 0, i6 - 0);
    }

    private void setLocation(int i, int i2) {
        int unifiedRotation = getUnifiedRotation();
        int i3 = i;
        int i4 = i2;
        int i5 = unifiedRotation;
        toIndex(this.mHelp0_0, i3, i4, i5, 1, 3, 0);
        toIndex(this.mHelp1_0, i3, i4, i5, 2, 2, 1);
        toIndex(this.mHelp3_0, i3, i4, i5, 3, 3, 2);
        toIndex(this.mHelp4_6, i3, i4, i5, 2, 1, 3);
        toIndex(this.mOk2_4, i3, i4, i5, 2, 5, 4);
        fillArrows(i, i2, unifiedRotation);
    }

    private void fillArrows(int i, int i2, int i3) {
        View view = new View(this.mContext);
        View view2 = new View(this.mContext);
        View view3 = new View(this.mContext);
        int i4 = i;
        int i5 = i2;
        int i6 = i3;
        toIndex(view, i4, i5, i6, 1, 3, -1);
        toIndex(view2, i4, i5, i6, 0, 1, -1);
        toIndex(view3, i4, i5, i6, 0, 0, -1);
        this.mArrows.addPath(new float[]{view.getX() - 50.0f, view2.getX(), view3.getX()}, new float[]{view.getY() - 50.0f, view2.getY(), view3.getY() + 50.0f});
        int i7 = i;
        toIndex(view, i7, i5, i6, 2, 2, -1);
        toIndex(view2, i7, i5, i6, 1, 3, -1);
        float f = this.mCurrentWidth;
        toIndex(view3, i + ((int) (f * 20.0f * 3.0f)), i2 - ((int) ((f * 20.0f) * 2.0f)), i6, 1, 6, -1);
        this.mArrows.addPath(new float[]{view.getX() + 50.0f, view2.getX(), view3.getX()}, new float[]{view.getY() + 50.0f, view2.getY(), view3.getY() - 50.0f});
        int i8 = i2;
        toIndex(view, i, i8, i6, 2, 1, -1);
        toIndex(view2, i - ((int) ((this.mCurrentWidth * 20.0f) * 2.0f)), i8, i6, 2, 0, -1);
        this.mArrows.addPath(new float[]{view.getX(), view2.getX()}, new float[]{view.getY() - 100.0f, view2.getY() + 50.0f});
        int i9 = i;
        int i10 = i2;
        toIndex(view, i9, i10, i6, 3, 3, -1);
        toIndex(view2, i9, i10, i6, 4, 1, -1);
        toIndex(view3, i9, i10, i6, 3, 0, -1);
        this.mArrows.addPath(new float[]{view.getX() + 50.0f, view2.getX(), view3.getX()}, new float[]{view.getY() - 50.0f, view2.getY(), view3.getY() + 50.0f});
    }

    private void toIndex(View view, int i, int i2, int i3, int i4, int i5, int i6) {
        int i7;
        int i8 = i3;
        int i9 = i6;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        int i10 = 0;
        int i11 = 7;
        int i12 = 5;
        if (i8 != 0) {
            if (i8 == 90) {
                i7 = (5 - i4) - 1;
                i10 = i5;
            } else if (i8 == 180) {
                i10 = (5 - i4) - 1;
                i7 = (7 - i5) - 1;
            } else if (i8 != 270) {
                i7 = 0;
            } else {
                i10 = (7 - i5) - 1;
                i7 = i4;
            }
            i12 = 7;
            i11 = 5;
        } else {
            i10 = i4;
            i7 = i5;
        }
        int i13 = (((i10 * 2) + 1) * (i / i12)) / 2;
        int i14 = (((i7 * 2) + 1) * (i2 / i11)) / 2;
        if (i5 == 0) {
            int i15 = mTopMargin;
            if (i15 != 0) {
                if (i8 == 90) {
                    i13 = i15 / 2;
                } else if (i8 == 180) {
                    i14 = i2 - (i15 / 2);
                } else if (i8 != 270) {
                    i14 = i15 / 2;
                } else {
                    i13 = i - (i15 / 2);
                }
            }
        }
        int i16 = measuredWidth / 2;
        int i17 = i13 - i16;
        int i18 = i13 + i16;
        int i19 = measuredHeight / 2;
        int i20 = i14 - i19;
        int i21 = i14 + i19;
        if (i9 != -1) {
            int i22 = i8 / 90;
            this.mLocX[i22][i9] = (float) i17;
            this.mLocY[i22][i9] = (float) i20;
        }
        View view2 = view;
        view.layout(i17, i20, i18, i21);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        for (RotateLayout orientation : new RotateLayout[]{this.mHelp0_0, this.mHelp1_0, this.mHelp3_0, this.mHelp4_6, this.mOk2_4}) {
            orientation.setOrientation(i, z);
        }
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundView = findViewById(C0905R.C0907id.background);
        this.mBackgroundView.setBackgroundColor(Color.argb(200, 0, 0, 0));
        this.mHelp0_0 = (RotateLayout) findViewById(C0905R.C0907id.help_text_0_0);
        fillHelp0_0();
        this.mHelp1_0 = (RotateLayout) findViewById(C0905R.C0907id.help_text_1_0);
        fillHelp1_0();
        this.mHelp3_0 = (RotateLayout) findViewById(C0905R.C0907id.help_text_3_0);
        fillHelp3_0();
        this.mHelp4_6 = (RotateLayout) findViewById(C0905R.C0907id.help_text_4_6);
        fillHelp4_6();
        this.mOk2_4 = (RotateLayout) findViewById(C0905R.C0907id.help_ok_2_4);
        fillOk2_4();
        this.mArrows = (Arrows) findViewById(C0905R.C0907id.arrows);
    }

    private void fillOk2_4() {
        LinearLayout linearLayout = new LinearLayout(this.mContext);
        this.mOk2_4.addView(linearLayout);
        linearLayout.setGravity(17);
        linearLayout.setPadding(40, 20, 40, 20);
        linearLayout.setBackgroundColor(-1);
        TextView textView = new TextView(this.mContext);
        textView.setText(getResources().getString(C0905R.string.help_menu_ok));
        textView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        textView.setTypeface(this.mTypeface);
        linearLayout.addView(textView);
    }

    private void fillHelp0_0() {
        TableLayout tableLayout = new TableLayout(this.mContext);
        this.mHelp0_0.addView(tableLayout);
        LinearLayout linearLayout = new LinearLayout(this.mContext);
        TextView textView = new TextView(this.mContext);
        textView.setTextColor(getResources().getColor(C0905R.color.help_menu_scene_mode_1));
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0905R.string.help_menu_scene_mode_1));
        sb.append(" ");
        textView.setText(sb.toString());
        textView.setTypeface(this.mTypeface);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this.mContext);
        textView2.setText(getResources().getString(C0905R.string.help_menu_scene_mode_2));
        textView2.setTypeface(this.mTypeface);
        linearLayout.addView(textView2);
        textView2.setTextColor(getResources().getColor(C0905R.color.help_menu_scene_mode_2));
        tableLayout.addView(linearLayout);
        TextView textView3 = new TextView(this.mContext);
        textView3.setText(getResources().getString(C0905R.string.help_menu_scene_mode_3));
        textView3.setTextColor(getResources().getColor(C0905R.color.help_menu_scene_mode_3));
        textView3.setTypeface(this.mTypeface);
        tableLayout.addView(textView3);
    }

    private void fillHelp1_0() {
        TableLayout tableLayout = new TableLayout(this.mContext);
        this.mHelp1_0.addView(tableLayout);
        LinearLayout linearLayout = new LinearLayout(this.mContext);
        TextView textView = new TextView(this.mContext);
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0905R.string.help_menu_color_filter_1));
        String str = " ";
        sb.append(str);
        textView.setText(sb.toString());
        textView.setTextColor(getResources().getColor(C0905R.color.help_menu_color_filter_1));
        textView.setTypeface(this.mTypeface);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this.mContext);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(getResources().getString(C0905R.string.help_menu_color_filter_2));
        sb2.append(str);
        textView2.setText(sb2.toString());
        textView2.setTextColor(getResources().getColor(C0905R.color.help_menu_color_filter_2));
        textView2.setTypeface(this.mTypeface);
        linearLayout.addView(textView2);
        TextView textView3 = new TextView(this.mContext);
        textView3.setText(getResources().getString(C0905R.string.help_menu_color_filter_3));
        textView3.setTextColor(getResources().getColor(C0905R.color.help_menu_color_filter_3));
        textView3.setTypeface(this.mTypeface);
        linearLayout.addView(textView3);
        tableLayout.addView(linearLayout);
        TextView textView4 = new TextView(this.mContext);
        textView4.setText(getResources().getString(C0905R.string.help_menu_color_filter_4));
        textView4.setTextColor(getResources().getColor(C0905R.color.help_menu_color_filter_4));
        textView4.setTypeface(this.mTypeface);
        tableLayout.addView(textView4);
    }

    private void fillHelp3_0() {
        TableLayout tableLayout = new TableLayout(this.mContext);
        this.mHelp3_0.addView(tableLayout);
        TextView textView = new TextView(this.mContext);
        textView.setText(getResources().getString(C0905R.string.help_menu_beautify_1));
        textView.setTextColor(getResources().getColor(C0905R.color.help_menu_beautify_1));
        textView.setTypeface(this.mTypeface);
        tableLayout.addView(textView);
        TextView textView2 = new TextView(this.mContext);
        textView2.setText(getResources().getString(C0905R.string.help_menu_beautify_2));
        textView2.setTextColor(getResources().getColor(C0905R.color.help_menu_beautify_2));
        textView2.setTypeface(this.mTypeface);
        tableLayout.addView(textView2);
        TextView textView3 = new TextView(this.mContext);
        textView3.setText(getResources().getString(C0905R.string.help_menu_beautify_3));
        textView3.setTextColor(getResources().getColor(C0905R.color.help_menu_beautify_3));
        textView3.setTypeface(this.mTypeface);
        tableLayout.addView(textView3);
    }

    private void fillHelp4_6() {
        TableLayout tableLayout = new TableLayout(this.mContext);
        this.mHelp4_6.addView(tableLayout);
        LinearLayout linearLayout = new LinearLayout(this.mContext);
        TextView textView = new TextView(this.mContext);
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(C0905R.string.help_menu_bokeh_1));
        sb.append(" ");
        textView.setText(sb.toString());
        textView.setTextColor(-1);
        textView.setTypeface(this.mTypeface);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this.mContext);
        textView2.setText(getResources().getString(C0905R.string.help_menu_bokeh_2));
        textView2.setTextColor(-16711936);
        textView2.setTypeface(this.mTypeface);
        linearLayout.addView(textView2);
        tableLayout.addView(linearLayout);
        TextView textView3 = new TextView(this.mContext);
        textView3.setText(getResources().getString(C0905R.string.help_menu_bokeh_3));
        textView3.setTextColor(-1);
        textView3.setTypeface(this.mTypeface);
        tableLayout.addView(textView3);
    }
}
