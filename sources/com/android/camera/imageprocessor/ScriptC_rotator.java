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

public class ScriptC_rotator extends ScriptC {
    private static final String __rs_resource_name = "rotator";
    private static final int mExportForEachIdx_rotate90andMerge = 1;
    private static final int mExportVarIdx_degree = 5;
    private static final int mExportVarIdx_gIn = 1;
    private static final int mExportVarIdx_gOut = 0;
    private static final int mExportVarIdx_height = 3;
    private static final int mExportVarIdx_pad = 4;
    private static final int mExportVarIdx_width = 2;
    private Element __ALLOCATION;
    private Element __U32;
    private Element __U8;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_U32;
    private long mExportVar_degree;
    private Allocation mExportVar_gIn;
    private Allocation mExportVar_gOut;
    private long mExportVar_height;
    private long mExportVar_pad;
    private long mExportVar_width;

    public ScriptC_rotator(RenderScript renderScript) {
        super(renderScript, __rs_resource_name, rotatorBitCode.getBitCode32(), rotatorBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(renderScript);
        this.__U32 = Element.U32(renderScript);
        this.__U8 = Element.U8(renderScript);
    }

    public synchronized void set_gOut(Allocation allocation) {
        setVar(0, allocation);
        this.mExportVar_gOut = allocation;
    }

    public Allocation get_gOut() {
        return this.mExportVar_gOut;
    }

    public FieldID getFieldID_gOut() {
        return createFieldID(0, null);
    }

    public synchronized void set_gIn(Allocation allocation) {
        setVar(1, allocation);
        this.mExportVar_gIn = allocation;
    }

    public Allocation get_gIn() {
        return this.mExportVar_gIn;
    }

    public FieldID getFieldID_gIn() {
        return createFieldID(1, null);
    }

    public synchronized void set_width(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(2, this.__rs_fp_U32);
        this.mExportVar_width = j;
    }

    public long get_width() {
        return this.mExportVar_width;
    }

    public FieldID getFieldID_width() {
        return createFieldID(2, null);
    }

    public synchronized void set_height(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(3, this.__rs_fp_U32);
        this.mExportVar_height = j;
    }

    public long get_height() {
        return this.mExportVar_height;
    }

    public FieldID getFieldID_height() {
        return createFieldID(3, null);
    }

    public synchronized void set_pad(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(4, this.__rs_fp_U32);
        this.mExportVar_pad = j;
    }

    public long get_pad() {
        return this.mExportVar_pad;
    }

    public FieldID getFieldID_pad() {
        return createFieldID(4, null);
    }

    public synchronized void set_degree(long j) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(j);
        setVar(5, this.__rs_fp_U32);
        this.mExportVar_degree = j;
    }

    public long get_degree() {
        return this.mExportVar_degree;
    }

    public FieldID getFieldID_degree() {
        return createFieldID(5, null);
    }

    public KernelID getKernelID_rotate90andMerge() {
        return createKernelID(1, 58, null, null);
    }

    public void forEach_rotate90andMerge(Allocation allocation) {
        forEach_rotate90andMerge(allocation, null);
    }

    public void forEach_rotate90andMerge(Allocation allocation, LaunchOptions launchOptions) {
        if (allocation.getType().getElement().isCompatible(this.__U8)) {
            forEach(1, null, allocation, null, launchOptions);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U8!");
    }
}
