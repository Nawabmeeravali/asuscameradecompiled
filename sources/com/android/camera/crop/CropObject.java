package com.android.camera.crop;

import android.graphics.Rect;
import android.graphics.RectF;

public class CropObject {
    public static final int BOTTOM_LEFT = 9;
    public static final int BOTTOM_RIGHT = 12;
    public static final int MOVE_BLOCK = 16;
    public static final int MOVE_BOTTOM = 8;
    public static final int MOVE_LEFT = 1;
    public static final int MOVE_NONE = 0;
    public static final int MOVE_RIGHT = 4;
    public static final int MOVE_TOP = 2;
    public static final int TOP_LEFT = 3;
    public static final int TOP_RIGHT = 6;
    private float mAspectHeight = 1.0f;
    private float mAspectWidth = 1.0f;
    private BoundedRect mBoundedRect;
    private boolean mFixAspectRatio = false;
    private float mMinSideSize = 20.0f;
    private int mMovingEdges = 0;
    private float mRotation = 0.0f;
    private float mTouchTolerance = 45.0f;

    public static boolean checkBlock(int i) {
        return i == 16;
    }

    public static boolean checkCorner(int i) {
        return i == 3 || i == 6 || i == 12 || i == 9;
    }

    public static boolean checkEdge(int i) {
        return i == 1 || i == 2 || i == 4 || i == 8;
    }

    private static int fixEdgeToCorner(int i) {
        if (i == 1) {
            i |= 2;
        }
        if (i == 2) {
            i |= 1;
        }
        if (i == 4) {
            i |= 8;
        }
        return i == 8 ? i | 4 : i;
    }

    public CropObject(Rect rect, Rect rect2, int i) {
        this.mBoundedRect = new BoundedRect((float) (i % 360), rect, rect2);
    }

    public CropObject(RectF rectF, RectF rectF2, int i) {
        this.mBoundedRect = new BoundedRect((float) (i % 360), rectF, rectF2);
    }

    public void resetBoundsTo(RectF rectF, RectF rectF2) {
        this.mBoundedRect.resetTo(0.0f, rectF2, rectF);
    }

    public void getInnerBounds(RectF rectF) {
        this.mBoundedRect.setToInner(rectF);
    }

    public void getOuterBounds(RectF rectF) {
        this.mBoundedRect.setToOuter(rectF);
    }

    public RectF getInnerBounds() {
        return this.mBoundedRect.getInner();
    }

    public RectF getOuterBounds() {
        return this.mBoundedRect.getOuter();
    }

    public int getSelectState() {
        return this.mMovingEdges;
    }

    public boolean isFixedAspect() {
        return this.mFixAspectRatio;
    }

    public void rotateOuter(int i) {
        this.mRotation = (float) (i % 360);
        this.mBoundedRect.setRotation(this.mRotation);
        clearSelectState();
    }

    public boolean setInnerAspectRatio(float f, float f2) {
        if (f <= 0.0f || f2 <= 0.0f) {
            throw new IllegalArgumentException("Width and Height must be greater than zero");
        }
        RectF inner = this.mBoundedRect.getInner();
        CropMath.fixAspectRatioContained(inner, f, f2);
        if (inner.width() < this.mMinSideSize || inner.height() < this.mMinSideSize) {
            return false;
        }
        this.mAspectWidth = f;
        this.mAspectHeight = f2;
        this.mFixAspectRatio = true;
        this.mBoundedRect.setInner(inner);
        clearSelectState();
        return true;
    }

    public void setTouchTolerance(float f) {
        if (f > 0.0f) {
            this.mTouchTolerance = f;
            return;
        }
        throw new IllegalArgumentException("Tolerance must be greater than zero");
    }

    public void setMinInnerSideSize(float f) {
        if (f > 0.0f) {
            this.mMinSideSize = f;
            return;
        }
        throw new IllegalArgumentException("Min dide must be greater than zero");
    }

    public void unsetAspectRatio() {
        this.mFixAspectRatio = false;
        clearSelectState();
    }

    public boolean hasSelectedEdge() {
        return this.mMovingEdges != 0;
    }

    public static boolean checkValid(int i) {
        return i == 0 || checkBlock(i) || checkEdge(i) || checkCorner(i);
    }

    public void clearSelectState() {
        this.mMovingEdges = 0;
    }

    public int wouldSelectEdge(float f, float f2) {
        int calculateSelectedEdge = calculateSelectedEdge(f, f2);
        if (calculateSelectedEdge == 0 || calculateSelectedEdge == 16) {
            return 0;
        }
        return calculateSelectedEdge;
    }

    public boolean selectEdge(int i) {
        if (!checkValid(i)) {
            throw new IllegalArgumentException("bad edge selected");
        } else if (!this.mFixAspectRatio || checkCorner(i) || checkBlock(i) || i == 0) {
            this.mMovingEdges = i;
            return true;
        } else {
            throw new IllegalArgumentException("bad corner selected");
        }
    }

    public boolean selectEdge(float f, float f2) {
        int calculateSelectedEdge = calculateSelectedEdge(f, f2);
        if (this.mFixAspectRatio) {
            calculateSelectedEdge = fixEdgeToCorner(calculateSelectedEdge);
        }
        if (calculateSelectedEdge == 0) {
            return false;
        }
        return selectEdge(calculateSelectedEdge);
    }

    public boolean moveCurrentSelection(float f, float f2) {
        if (this.mMovingEdges == 0) {
            return false;
        }
        RectF inner = this.mBoundedRect.getInner();
        float f3 = this.mMinSideSize;
        int i = this.mMovingEdges;
        if (i == 16) {
            this.mBoundedRect.moveInner(f, f2);
            return true;
        }
        int i2 = i & 1;
        float f4 = 0.0f;
        float min = i2 != 0 ? Math.min(inner.left + f, inner.right - f3) - inner.left : 0.0f;
        int i3 = i & 2;
        if (i3 != 0) {
            f4 = Math.min(inner.top + f2, inner.bottom - f3) - inner.top;
        }
        int i4 = i & 4;
        if (i4 != 0) {
            min = Math.max(inner.right + f, inner.left + f3) - inner.right;
        }
        int i5 = i & 8;
        if (i5 != 0) {
            f4 = Math.max(inner.bottom + f2, inner.top + f3) - inner.bottom;
        }
        if (this.mFixAspectRatio) {
            float[] fArr = {inner.left, inner.bottom};
            float[] fArr2 = {inner.right, inner.top};
            if (i == 3 || i == 12) {
                fArr[1] = inner.top;
                fArr2[1] = inner.bottom;
            }
            float[] fArr3 = {min, f4};
            float[] normalize = GeometryMathUtils.normalize(new float[]{fArr[0] - fArr2[0], fArr[1] - fArr2[1]});
            float scalarProjection = GeometryMathUtils.scalarProjection(fArr3, normalize);
            this.mBoundedRect.fixedAspectResizeInner(fixedCornerResize(inner, i, normalize[0] * scalarProjection, scalarProjection * normalize[1]));
        } else {
            if (i2 != 0) {
                inner.left += min;
            }
            if (i3 != 0) {
                inner.top += f4;
            }
            if (i4 != 0) {
                inner.right += min;
            }
            if (i5 != 0) {
                inner.bottom += f4;
            }
            this.mBoundedRect.resizeInner(inner);
        }
        return true;
    }

    private int calculateSelectedEdge(float f, float f2) {
        int i;
        RectF inner = this.mBoundedRect.getInner();
        float abs = Math.abs(f - inner.left);
        float abs2 = Math.abs(f - inner.right);
        float abs3 = Math.abs(f2 - inner.top);
        float abs4 = Math.abs(f2 - inner.bottom);
        float f3 = this.mTouchTolerance;
        if (abs > f3 || f2 + f3 < inner.top || f2 - f3 > inner.bottom || abs >= abs2) {
            float f4 = this.mTouchTolerance;
            i = (abs2 > f4 || f2 + f4 < inner.top || f2 - f4 > inner.bottom) ? 0 : 4;
        } else {
            i = 1;
        }
        float f5 = this.mTouchTolerance;
        if (abs3 <= f5 && f + f5 >= inner.left && f - f5 <= inner.right && abs3 < abs4) {
            return i | 2;
        }
        float f6 = this.mTouchTolerance;
        return (abs4 > f6 || f + f6 < inner.left || f - f6 > inner.right) ? i : i | 8;
    }

    private static RectF fixedCornerResize(RectF rectF, int i, float f, float f2) {
        if (i == 12) {
            float f3 = rectF.left;
            return new RectF(f3, rectF.top, rectF.width() + f3 + f, rectF.top + rectF.height() + f2);
        } else if (i == 9) {
            float width = (rectF.right - rectF.width()) + f;
            float f4 = rectF.top;
            return new RectF(width, f4, rectF.right, rectF.height() + f4 + f2);
        } else if (i == 3) {
            return new RectF((rectF.right - rectF.width()) + f, (rectF.bottom - rectF.height()) + f2, rectF.right, rectF.bottom);
        } else {
            if (i == 6) {
                return new RectF(rectF.left, (rectF.bottom - rectF.height()) + f2, rectF.left + rectF.width() + f, rectF.bottom);
            }
            return null;
        }
    }
}
