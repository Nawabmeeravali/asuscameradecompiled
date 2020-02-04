package com.android.camera;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Log;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class SurfaceTextureRenderer {
    private static final int[] CONFIG_SPEC = {12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12344};
    private static final int EGL_CONTEXT_CLIENT_VERSION = 12440;
    private static final int EGL_OPENGL_ES2_BIT = 4;
    /* access modifiers changed from: private */
    public static final String TAG;
    /* access modifiers changed from: private */
    public EGL10 mEgl;
    /* access modifiers changed from: private */
    public EGLConfig mEglConfig;
    /* access modifiers changed from: private */
    public EGLContext mEglContext;
    /* access modifiers changed from: private */
    public EGLDisplay mEglDisplay;
    private Handler mEglHandler;
    /* access modifiers changed from: private */
    public EGLSurface mEglSurface;
    /* access modifiers changed from: private */
    public FrameDrawer mFrameDrawer;
    /* access modifiers changed from: private */
    public GL10 mGl;
    /* access modifiers changed from: private */
    public Object mRenderLock = new Object();
    private Runnable mRenderTask = new Runnable() {
        public void run() {
            synchronized (SurfaceTextureRenderer.this.mRenderLock) {
                if (!(SurfaceTextureRenderer.this.mEglDisplay == null || SurfaceTextureRenderer.this.mEglSurface == null)) {
                    SurfaceTextureRenderer.this.mFrameDrawer.onDrawFrame(SurfaceTextureRenderer.this.mGl);
                    SurfaceTextureRenderer.this.mEgl.eglSwapBuffers(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglSurface);
                }
                SurfaceTextureRenderer.this.mRenderLock.notifyAll();
            }
        }
    };

    public interface FrameDrawer {
        void onDrawFrame(GL10 gl10);
    }

    public class RenderThread extends Thread {
        private Boolean mRenderStopped = Boolean.valueOf(false);

        public RenderThread() {
        }

        public void run() {
            while (true) {
                synchronized (this.mRenderStopped) {
                    if (this.mRenderStopped.booleanValue()) {
                        return;
                    }
                }
                SurfaceTextureRenderer.this.draw(true);
            }
            while (true) {
            }
        }

        public void stopRender() {
            synchronized (this.mRenderStopped) {
                this.mRenderStopped = Boolean.valueOf(true);
            }
        }
    }

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("CAM_");
        sb.append(SurfaceTextureRenderer.class.getSimpleName());
        TAG = sb.toString();
    }

    public SurfaceTextureRenderer(SurfaceTexture surfaceTexture, Handler handler, FrameDrawer frameDrawer) {
        this.mEglHandler = handler;
        this.mFrameDrawer = frameDrawer;
        initialize(surfaceTexture);
    }

    public RenderThread createRenderThread() {
        return new RenderThread();
    }

    public void release() {
        this.mEglHandler.post(new Runnable() {
            public void run() {
                SurfaceTextureRenderer.this.mEgl.eglDestroySurface(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglSurface);
                SurfaceTextureRenderer.this.mEgl.eglDestroyContext(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglContext);
                EGL10 access$500 = SurfaceTextureRenderer.this.mEgl;
                EGLDisplay access$100 = SurfaceTextureRenderer.this.mEglDisplay;
                EGLSurface eGLSurface = EGL10.EGL_NO_SURFACE;
                access$500.eglMakeCurrent(access$100, eGLSurface, eGLSurface, EGL10.EGL_NO_CONTEXT);
                SurfaceTextureRenderer.this.mEgl.eglTerminate(SurfaceTextureRenderer.this.mEglDisplay);
                SurfaceTextureRenderer.this.mEglSurface = null;
                SurfaceTextureRenderer.this.mEglContext = null;
                SurfaceTextureRenderer.this.mEglDisplay = null;
            }
        });
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(4:5|6|7|8) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0012 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void draw(boolean r4) {
        /*
            r3 = this;
            java.lang.Object r0 = r3.mRenderLock
            monitor-enter(r0)
            android.os.Handler r1 = r3.mEglHandler     // Catch:{ all -> 0x001b }
            java.lang.Runnable r2 = r3.mRenderTask     // Catch:{ all -> 0x001b }
            r1.post(r2)     // Catch:{ all -> 0x001b }
            if (r4 == 0) goto L_0x0019
            java.lang.Object r3 = r3.mRenderLock     // Catch:{ InterruptedException -> 0x0012 }
            r3.wait()     // Catch:{ InterruptedException -> 0x0012 }
            goto L_0x0019
        L_0x0012:
            java.lang.String r3 = TAG     // Catch:{ all -> 0x001b }
            java.lang.String r4 = "RenderLock.wait() interrupted"
            android.util.Log.v(r3, r4)     // Catch:{ all -> 0x001b }
        L_0x0019:
            monitor-exit(r0)     // Catch:{ all -> 0x001b }
            return
        L_0x001b:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001b }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.SurfaceTextureRenderer.draw(boolean):void");
    }

    private void initialize(final SurfaceTexture surfaceTexture) {
        this.mEglHandler.post(new Runnable() {
            public void run() {
                SurfaceTextureRenderer.this.mEgl = (EGL10) EGLContext.getEGL();
                SurfaceTextureRenderer surfaceTextureRenderer = SurfaceTextureRenderer.this;
                surfaceTextureRenderer.mEglDisplay = surfaceTextureRenderer.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
                if (SurfaceTextureRenderer.this.mEglDisplay != EGL10.EGL_NO_DISPLAY) {
                    int[] iArr = new int[2];
                    if (SurfaceTextureRenderer.this.mEgl.eglInitialize(SurfaceTextureRenderer.this.mEglDisplay, iArr)) {
                        String access$700 = SurfaceTextureRenderer.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("EGL version: ");
                        sb.append(iArr[0]);
                        sb.append('.');
                        sb.append(iArr[1]);
                        Log.v(access$700, sb.toString());
                        int[] iArr2 = {SurfaceTextureRenderer.EGL_CONTEXT_CLIENT_VERSION, 2, 12344};
                        SurfaceTextureRenderer surfaceTextureRenderer2 = SurfaceTextureRenderer.this;
                        surfaceTextureRenderer2.mEglConfig = SurfaceTextureRenderer.chooseConfig(surfaceTextureRenderer2.mEgl, SurfaceTextureRenderer.this.mEglDisplay);
                        SurfaceTextureRenderer surfaceTextureRenderer3 = SurfaceTextureRenderer.this;
                        surfaceTextureRenderer3.mEglContext = surfaceTextureRenderer3.mEgl.eglCreateContext(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglConfig, EGL10.EGL_NO_CONTEXT, iArr2);
                        if (SurfaceTextureRenderer.this.mEglContext == null || SurfaceTextureRenderer.this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                            throw new RuntimeException("failed to createContext");
                        }
                        SurfaceTextureRenderer surfaceTextureRenderer4 = SurfaceTextureRenderer.this;
                        surfaceTextureRenderer4.mEglSurface = surfaceTextureRenderer4.mEgl.eglCreateWindowSurface(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglConfig, surfaceTexture, null);
                        if (SurfaceTextureRenderer.this.mEglSurface == null || SurfaceTextureRenderer.this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                            throw new RuntimeException("failed to createWindowSurface");
                        } else if (SurfaceTextureRenderer.this.mEgl.eglMakeCurrent(SurfaceTextureRenderer.this.mEglDisplay, SurfaceTextureRenderer.this.mEglSurface, SurfaceTextureRenderer.this.mEglSurface, SurfaceTextureRenderer.this.mEglContext)) {
                            SurfaceTextureRenderer surfaceTextureRenderer5 = SurfaceTextureRenderer.this;
                            surfaceTextureRenderer5.mGl = (GL10) surfaceTextureRenderer5.mEglContext.getGL();
                        } else {
                            throw new RuntimeException("failed to eglMakeCurrent");
                        }
                    } else {
                        throw new RuntimeException("eglInitialize failed");
                    }
                } else {
                    throw new RuntimeException("eglGetDisplay failed");
                }
            }
        });
        waitDone();
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:2|3|4|5|6|7|8) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:6:0x0014 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitDone() {
        /*
            r3 = this;
            java.lang.Object r0 = new java.lang.Object
            r0.<init>()
            monitor-enter(r0)
            android.os.Handler r1 = r3.mEglHandler     // Catch:{ all -> 0x001d }
            com.android.camera.SurfaceTextureRenderer$4 r2 = new com.android.camera.SurfaceTextureRenderer$4     // Catch:{ all -> 0x001d }
            r2.<init>(r0)     // Catch:{ all -> 0x001d }
            r1.post(r2)     // Catch:{ all -> 0x001d }
            r0.wait()     // Catch:{ InterruptedException -> 0x0014 }
            goto L_0x001b
        L_0x0014:
            java.lang.String r3 = TAG     // Catch:{ all -> 0x001d }
            java.lang.String r1 = "waitDone() interrupted"
            android.util.Log.v(r3, r1)     // Catch:{ all -> 0x001d }
        L_0x001b:
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            return
        L_0x001d:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.camera.SurfaceTextureRenderer.waitDone():void");
    }

    private static void checkEglError(String str, EGL10 egl10) {
        while (true) {
            int eglGetError = egl10.eglGetError();
            if (eglGetError != 12288) {
                Log.e(TAG, String.format("%s: EGL error: 0x%x", new Object[]{str, Integer.valueOf(eglGetError)}));
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay) {
        int[] iArr = new int[1];
        if (egl10.eglChooseConfig(eGLDisplay, CONFIG_SPEC, null, 0, iArr)) {
            int i = iArr[0];
            if (i > 0) {
                EGLConfig[] eGLConfigArr = new EGLConfig[i];
                if (egl10.eglChooseConfig(eGLDisplay, CONFIG_SPEC, eGLConfigArr, i, iArr)) {
                    return eGLConfigArr[0];
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("No configs match configSpec");
        }
        throw new IllegalArgumentException("eglChooseConfig failed");
    }
}
