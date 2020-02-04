package androidx.heifwriter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.FloatBuffer;

public class Texture2dProgram {
    private static final boolean DEBUG = false;
    private static final String FRAGMENT_SHADER_2D = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
    private static final String FRAGMENT_SHADER_EXT = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
    public static final float[] IDENTITY_MATRIX = new float[16];
    private static final String TAG = "Texture2dProgram";
    public static final int TEXTURE_2D = 0;
    public static final int TEXTURE_EXT = 1;
    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n";
    public static final float[] V_FLIP_MATRIX = new float[16];
    private int mProgramHandle;
    private int mProgramType;
    private int mTextureTarget;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgramType {
    }

    static {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
        Matrix.setIdentityM(V_FLIP_MATRIX, 0);
        Matrix.translateM(V_FLIP_MATRIX, 0, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(V_FLIP_MATRIX, 0, 1.0f, -1.0f, 1.0f);
    }

    public Texture2dProgram(int i) {
        this.mProgramType = i;
        String str = VERTEX_SHADER;
        if (i == 0) {
            this.mTextureTarget = 3553;
            this.mProgramHandle = createProgram(str, FRAGMENT_SHADER_2D);
        } else if (i == 1) {
            this.mTextureTarget = 36197;
            this.mProgramHandle = createProgram(str, FRAGMENT_SHADER_EXT);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unhandled type ");
            sb.append(i);
            throw new RuntimeException(sb.toString());
        }
        int i2 = this.mProgramHandle;
        if (i2 != 0) {
            String str2 = "aPosition";
            this.maPositionLoc = GLES20.glGetAttribLocation(i2, str2);
            checkLocation(this.maPositionLoc, str2);
            String str3 = "aTextureCoord";
            this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.mProgramHandle, str3);
            checkLocation(this.maTextureCoordLoc, str3);
            String str4 = "uMVPMatrix";
            this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.mProgramHandle, str4);
            checkLocation(this.muMVPMatrixLoc, str4);
            String str5 = "uTexMatrix";
            this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.mProgramHandle, str5);
            checkLocation(this.muTexMatrixLoc, str5);
            return;
        }
        throw new RuntimeException("Unable to create program");
    }

    public void release() {
        StringBuilder sb = new StringBuilder();
        sb.append("deleting program ");
        sb.append(this.mProgramHandle);
        Log.d(TAG, sb.toString());
        GLES20.glDeleteProgram(this.mProgramHandle);
        this.mProgramHandle = -1;
    }

    public int getProgramType() {
        return this.mProgramType;
    }

    public int createTextureObject() {
        int[] iArr = new int[1];
        GLES20.glGenTextures(1, iArr, 0);
        checkGlError("glGenTextures");
        int i = iArr[0];
        GLES20.glBindTexture(this.mTextureTarget, i);
        StringBuilder sb = new StringBuilder();
        sb.append("glBindTexture ");
        sb.append(i);
        checkGlError(sb.toString());
        float f = 9728.0f;
        GLES20.glTexParameterf(this.mTextureTarget, 10241, 9728.0f);
        int i2 = this.mTextureTarget;
        if (i2 != 3553) {
            f = 9729.0f;
        }
        GLES20.glTexParameterf(i2, 10240, f);
        GLES20.glTexParameteri(this.mTextureTarget, 10242, 33071);
        GLES20.glTexParameteri(this.mTextureTarget, 10243, 33071);
        checkGlError("glTexParameter");
        return i;
    }

    public void loadTexture(int i, Bitmap bitmap) {
        GLES20.glBindTexture(this.mTextureTarget, i);
        GLUtils.texImage2D(this.mTextureTarget, 0, bitmap, 0);
    }

    public void draw(float[] fArr, FloatBuffer floatBuffer, int i, int i2, int i3, int i4, float[] fArr2, FloatBuffer floatBuffer2, int i5, int i6) {
        checkGlError("draw start");
        GLES20.glUseProgram(this.mProgramHandle);
        checkGlError("glUseProgram");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(this.mTextureTarget, i5);
        float[] fArr3 = fArr;
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, fArr, 0);
        String str = "glUniformMatrix4fv";
        checkGlError(str);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, fArr2, 0);
        checkGlError(str);
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        String str2 = "glEnableVertexAttribArray";
        checkGlError(str2);
        GLES20.glVertexAttribPointer(this.maPositionLoc, i3, 5126, false, i4, floatBuffer);
        String str3 = "glVertexAttribPointer";
        checkGlError(str3);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLoc);
        checkGlError(str2);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, 5126, false, i6, floatBuffer2);
        checkGlError(str3);
        int i7 = i;
        int i8 = i2;
        GLES20.glDrawArrays(5, i, i2);
        checkGlError("glDrawArrays");
        GLES20.glDisableVertexAttribArray(this.maPositionLoc);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLoc);
        GLES20.glBindTexture(this.mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }

    public static int createProgram(String str, String str2) {
        int loadShader = loadShader(35633, str);
        int i = 0;
        if (loadShader == 0) {
            return 0;
        }
        int loadShader2 = loadShader(35632, str2);
        if (loadShader2 == 0) {
            return 0;
        }
        int glCreateProgram = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        String str3 = TAG;
        if (glCreateProgram == 0) {
            Log.e(str3, "Could not create program");
        }
        GLES20.glAttachShader(glCreateProgram, loadShader);
        String str4 = "glAttachShader";
        checkGlError(str4);
        GLES20.glAttachShader(glCreateProgram, loadShader2);
        checkGlError(str4);
        GLES20.glLinkProgram(glCreateProgram);
        int[] iArr = new int[1];
        GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
        if (iArr[0] != 1) {
            Log.e(str3, "Could not link program: ");
            Log.e(str3, GLES20.glGetProgramInfoLog(glCreateProgram));
            GLES20.glDeleteProgram(glCreateProgram);
        } else {
            i = glCreateProgram;
        }
        return i;
    }

    public static int loadShader(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        StringBuilder sb = new StringBuilder();
        sb.append("glCreateShader type=");
        sb.append(i);
        checkGlError(sb.toString());
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glCompileShader(glCreateShader);
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return glCreateShader;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Could not compile shader ");
        sb2.append(i);
        sb2.append(":");
        String sb3 = sb2.toString();
        String str2 = TAG;
        Log.e(str2, sb3);
        StringBuilder sb4 = new StringBuilder();
        sb4.append(" ");
        sb4.append(GLES20.glGetShaderInfoLog(glCreateShader));
        Log.e(str2, sb4.toString());
        GLES20.glDeleteShader(glCreateShader);
        return 0;
    }

    public static void checkLocation(int i, String str) {
        if (i < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to locate '");
            sb.append(str);
            sb.append("' in program");
            throw new RuntimeException(sb.toString());
        }
    }

    public static void checkGlError(String str) {
        int glGetError = GLES20.glGetError();
        String str2 = TAG;
        if (glGetError == 1285) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" GL_OUT_OF_MEMORY");
            Log.i(str2, sb.toString());
        }
        if (glGetError != 0 && glGetError != 1285) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(": glError 0x");
            sb2.append(Integer.toHexString(glGetError));
            String sb3 = sb2.toString();
            Log.e(str2, sb3);
            throw new RuntimeException(sb3);
        }
    }
}
