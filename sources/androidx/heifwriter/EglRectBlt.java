package androidx.heifwriter;

import android.graphics.Bitmap;
import android.graphics.Rect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class EglRectBlt {
    private static final FloatBuffer FULL_RECTANGLE_BUF = createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final float[] FULL_RECTANGLE_COORDS = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private static final int SIZEOF_FLOAT = 4;
    private Texture2dProgram mProgram;
    private final FloatBuffer mTexCoordArray = createFloatBuffer(this.mTexCoords);
    private final float[] mTexCoords = new float[8];
    private final int mTexHeight;
    private final int mTexWidth;

    public static FloatBuffer createFloatBuffer(float[] fArr) {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        FloatBuffer asFloatBuffer = allocateDirect.asFloatBuffer();
        asFloatBuffer.put(fArr);
        asFloatBuffer.position(0);
        return asFloatBuffer;
    }

    public EglRectBlt(Texture2dProgram texture2dProgram, int i, int i2) {
        this.mProgram = texture2dProgram;
        this.mTexWidth = i;
        this.mTexHeight = i2;
    }

    public void release(boolean z) {
        Texture2dProgram texture2dProgram = this.mProgram;
        if (texture2dProgram != null) {
            if (z) {
                texture2dProgram.release();
            }
            this.mProgram = null;
        }
    }

    public int createTextureObject() {
        return this.mProgram.createTextureObject();
    }

    public void loadTexture(int i, Bitmap bitmap) {
        this.mProgram.loadTexture(i, bitmap);
    }

    public void copyRect(int i, float[] fArr, Rect rect) {
        setTexRect(rect);
        this.mProgram.draw(Texture2dProgram.IDENTITY_MATRIX, FULL_RECTANGLE_BUF, 0, 4, 2, 8, fArr, this.mTexCoordArray, i, 8);
    }

    /* access modifiers changed from: 0000 */
    public void setTexRect(Rect rect) {
        float[] fArr = this.mTexCoords;
        int i = rect.left;
        float f = (float) i;
        int i2 = this.mTexWidth;
        fArr[0] = f / ((float) i2);
        int i3 = rect.bottom;
        float f2 = (float) i3;
        int i4 = this.mTexHeight;
        fArr[1] = 1.0f - (f2 / ((float) i4));
        int i5 = rect.right;
        fArr[2] = ((float) i5) / ((float) i2);
        fArr[3] = 1.0f - (((float) i3) / ((float) i4));
        fArr[4] = ((float) i) / ((float) i2);
        int i6 = rect.top;
        fArr[5] = 1.0f - (((float) i6) / ((float) i4));
        fArr[6] = ((float) i5) / ((float) i2);
        fArr[7] = 1.0f - (((float) i6) / ((float) i4));
        this.mTexCoordArray.put(fArr);
        this.mTexCoordArray.position(0);
    }
}
