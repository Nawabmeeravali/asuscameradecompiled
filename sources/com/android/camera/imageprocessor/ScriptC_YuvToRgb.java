package com.android.camera.imageprocessor;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;
import android.renderscript.ScriptC;

public class ScriptC_YuvToRgb extends ScriptC {
    private static final String __rs_resource_name = "yuvtorgb";
    private static final int mExportForEachIdx_nv21ToRgb = 1;
    private static final int mExportVarIdx_gIn = 0;
    private static final int mExportVarIdx_height = 2;
    private static final int mExportVarIdx_width = 1;
    private Element __ALLOCATION;
    private Element __U32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_U32;
    private Allocation mExportVar_gIn;
    private long mExportVar_height;
    private long mExportVar_width;

    public ScriptC_YuvToRgb(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, YuvToRgbBitCode.getBitCode32(), YuvToRgbBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__U32 = Element.U32(renderScript);
        this.__U8_4 = Element.U8_4(renderScript);
    }

    public synchronized void set_gIn(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_gIn = allocation;
    }

    public Allocation get_gIn() {
        return this.mExportVar_gIn;
    }

    public FieldID getFieldID_gIn() {
        return createFieldID(0, null);
    }

    public synchronized void set_width(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(1, this.__rs_fp_U32);
        this.mExportVar_width = j;
    }

    public long get_width() {
        return this.mExportVar_width;
    }

    public FieldID getFieldID_width() {
        return createFieldID(1, null);
    }

    public synchronized void set_height(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(2, this.__rs_fp_U32);
        this.mExportVar_height = j;
    }

    public long get_height() {
        return this.mExportVar_height;
    }

    public FieldID getFieldID_height() {
        return createFieldID(2, null);
    }

    public KernelID getKernelID_nv21ToRgb() {
        return createKernelID(1, 58, null, null);
    }

    public void forEach_nv21ToRgb(Allocation allocation) {
        forEach_nv21ToRgb(allocation, null);
    }

    public void forEach_nv21ToRgb(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8_4)) {
            forEach(1, null, allocation, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8_4!");
    }
}
