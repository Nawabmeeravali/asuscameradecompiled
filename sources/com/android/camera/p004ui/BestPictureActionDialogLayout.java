package com.android.camera.p004ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.codeaurora.snapcam.C0905R;

/* renamed from: com.android.camera.ui.BestPictureActionDialogLayout */
public class BestPictureActionDialogLayout extends RelativeLayout implements OnClickListener {
    private TextView mContent;
    private IDialogDataControler mDialogDataControler;
    private Button mNativeBt;
    private Button mOKButton;
    private Button mPositiveBt;
    private TextView mTitleText;
    int mode;

    /* renamed from: com.android.camera.ui.BestPictureActionDialogLayout$IDialogDataControler */
    public interface IDialogDataControler {
        void doClickNativeBtAction();

        void doClickOKBtAction();

        void doClickPositionBtAction();

        String getContentString();

        String getNativeButtonString();

        String getOKButtonString();

        String getPositionButtonString();

        String getTitleString();
    }

    public BestPictureActionDialogLayout(Context context) {
        this(context, null);
    }

    public BestPictureActionDialogLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BestPictureActionDialogLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setDialogDataControler(View view, IDialogDataControler iDialogDataControler) {
        this.mode = ((Integer) view.getTag()).intValue();
        this.mTitleText = (TextView) view.findViewById(C0905R.C0907id.mtitle);
        this.mContent = (TextView) view.findViewById(C0905R.C0907id.content);
        this.mNativeBt = (Button) view.findViewById(C0905R.C0907id.nativebt);
        this.mPositiveBt = (Button) view.findViewById(C0905R.C0907id.positivebt);
        this.mNativeBt.setOnClickListener(this);
        this.mPositiveBt.setOnClickListener(this);
        this.mOKButton = (Button) view.findViewById(C0905R.C0907id.okbt);
        this.mOKButton.setOnClickListener(this);
        this.mDialogDataControler = iDialogDataControler;
        setViewData();
    }

    private void setViewData() {
        this.mTitleText.setText(this.mDialogDataControler.getTitleString());
        this.mContent.setText(this.mDialogDataControler.getContentString());
        int i = this.mode;
        if (i == 1) {
            this.mNativeBt.setText(this.mDialogDataControler.getNativeButtonString());
            this.mPositiveBt.setText(this.mDialogDataControler.getPositionButtonString());
            this.mOKButton.setVisibility(8);
            this.mNativeBt.setVisibility(0);
            this.mPositiveBt.setVisibility(0);
        } else if (i == 0) {
            this.mOKButton.setText(this.mDialogDataControler.getOKButtonString());
            this.mOKButton.setVisibility(0);
            this.mNativeBt.setVisibility(8);
            this.mPositiveBt.setVisibility(8);
        }
        invalidate();
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == C0905R.C0907id.nativebt) {
            this.mDialogDataControler.doClickNativeBtAction();
        } else if (id == C0905R.C0907id.okbt) {
            this.mDialogDataControler.doClickOKBtAction();
        } else if (id == C0905R.C0907id.positivebt) {
            this.mDialogDataControler.doClickPositionBtAction();
        }
    }
}
