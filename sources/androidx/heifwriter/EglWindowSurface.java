package androidx.heifwriter;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;
import java.util.Objects;

public class EglWindowSurface {
    private static final String TAG = "EglWindowSurface";
    private EGLConfig[] mConfigs = new EGLConfig[1];
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private int mHeight;
    private Surface mSurface;
    private int mWidth;

    public EglWindowSurface(Surface surface) {
        if (surface != null) {
            this.mSurface = surface;
            eglSetup();
            return;
        }
        throw new NullPointerException();
    }

    private void eglSetup() {
        this.mEGLDisplay = EGL14.eglGetDisplay(0);
        if (!Objects.equals(this.mEGLDisplay, EGL14.EGL_NO_DISPLAY)) {
            int[] iArr = new int[2];
            if (EGL14.eglInitialize(this.mEGLDisplay, iArr, 0, iArr, 1)) {
                int[] iArr2 = {12324, 8, 12323, 8, 12322, 8, 12352, 4, 12610, 1, 12344};
                int[] iArr3 = new int[1];
                EGLDisplay eGLDisplay = this.mEGLDisplay;
                EGLConfig[] eGLConfigArr = this.mConfigs;
                if (EGL14.eglChooseConfig(eGLDisplay, iArr2, 0, eGLConfigArr, 0, eGLConfigArr.length, iArr3, 0)) {
                    this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, this.mConfigs[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
                    checkEglError("eglCreateContext");
                    if (this.mEGLContext != null) {
                        createEGLSurface();
                        this.mWidth = getWidth();
                        this.mHeight = getHeight();
                        return;
                    }
                    throw new RuntimeException("null context");
                }
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }
            this.mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        throw new RuntimeException("unable to get EGL14 display");
    }

    public void updateSize(int i, int i2) {
        if (i != this.mWidth || i2 != this.mHeight) {
            Log.d(TAG, "re-create EGLSurface");
            releaseEGLSurface();
            createEGLSurface();
            this.mWidth = getWidth();
            this.mHeight = getHeight();
        }
    }

    private void createEGLSurface() {
        this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, this.mConfigs[0], this.mSurface, new int[]{12344}, 0);
        checkEglError("eglCreateWindowSurface");
        if (this.mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    private void releaseEGLSurface() {
        if (!Objects.equals(this.mEGLDisplay, EGL14.EGL_NO_DISPLAY)) {
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public void release() {
        if (!Objects.equals(this.mEGLDisplay, EGL14.EGL_NO_DISPLAY)) {
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mSurface.release();
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mSurface = null;
    }

    public void makeCurrent() {
        EGLDisplay eGLDisplay = this.mEGLDisplay;
        EGLSurface eGLSurface = this.mEGLSurface;
        if (!EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, this.mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public void makeUnCurrent() {
        EGLDisplay eGLDisplay = this.mEGLDisplay;
        EGLSurface eGLSurface = EGL14.EGL_NO_SURFACE;
        if (!EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public boolean swapBuffers() {
        return EGL14.eglSwapBuffers(this.mEGLDisplay, this.mEGLSurface);
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public int getWidth() {
        int[] iArr = new int[1];
        EGL14.eglQuerySurface(this.mEGLDisplay, this.mEGLSurface, 12375, iArr, 0);
        return iArr[0];
    }

    public int getHeight() {
        int[] iArr = new int[1];
        EGL14.eglQuerySurface(this.mEGLDisplay, this.mEGLSurface, 12374, iArr, 0);
        return iArr[0];
    }

    public void setPresentationTime(long j) {
        EGLExt.eglPresentationTimeANDROID(this.mEGLDisplay, this.mEGLSurface, j);
    }

    private void checkEglError(String str) {
        int eglGetError = EGL14.eglGetError();
        if (eglGetError != 12288) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(": EGL error: 0x");
            sb.append(Integer.toHexString(eglGetError));
            throw new RuntimeException(sb.toString());
        }
    }
}
