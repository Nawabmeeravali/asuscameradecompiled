package com.asus.scenedetectlib.detector;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.asus.scenedetectlib.AISceneDetectInterface.SceneType;
import com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class SceneDetector extends Detector {

    /* renamed from: a */
    private SparseArray<BitmapPair> f94a;

    /* renamed from: b */
    private List<String> f95b;

    /* renamed from: c */
    private List<String> f96c;

    /* renamed from: d */
    private Matrix f97d;

    /* renamed from: e */
    private float[] f98e;

    /* renamed from: f */
    private int[] f99f;

    /* renamed from: g */
    private float[] f100g;

    /* renamed from: h */
    private float[] f101h;

    /* renamed from: i */
    private boolean f102i;

    /* renamed from: j */
    private LimitedQueue<String> f103j;

    /* renamed from: k */
    private ArrayMap<String, Integer> f104k;

    /* renamed from: l */
    private TensorFlowInferenceInterface f105l;

    /* renamed from: m */
    private List<String> f106m;

    /* renamed from: n */
    private float[] f107n;

    /* renamed from: o */
    private int[] f108o;

    /* renamed from: p */
    private float[] f109p;

    /* renamed from: q */
    private TensorFlowInferenceInterface f110q;

    private static class BitmapPair {

        /* renamed from: a */
        Bitmap f111a;

        /* renamed from: b */
        Canvas f112b;

        BitmapPair(Bitmap bitmap, Canvas canvas) {
            this.f111a = bitmap;
            this.f112b = canvas;
        }
    }

    private static class LimitedQueue<E> extends LinkedList<E> {

        /* renamed from: a */
        private int f113a;

        LimitedQueue(int i) {
            this.f113a = i;
        }

        public boolean add(E e) {
            super.add(e);
            while (size() > this.f113a) {
                super.remove();
            }
            return true;
        }
    }

    static {
        System.loadLibrary("scene-native-lib");
    }

    public SceneDetector(Context context) {
        this(context, false);
    }

    public SceneDetector(Context context, boolean z) {
        super(context);
        this.f94a = new SparseArray<>();
        this.f97d = new Matrix();
        this.f102i = false;
        this.f102i = z;
        AssetManager assets = context.getAssets();
        this.f95b = m17a(assets, "single_labels.txt");
        this.f96c = m17a(assets, "multi_labels.txt");
        this.f105l = loadModel(context, "model_181113_0");
        String str = "Failed on manufacturer check.";
        if (this.f105l != null) {
            String str2 = "SceneDetector";
            Log.i(str2, "Load model \"model_181113_0\" successfully.");
            this.f98e = new float[150528];
            this.f99f = new int[50176];
            this.f100g = new float[this.f95b.size()];
            this.f101h = new float[this.f96c.size()];
            this.f106m = m17a(assets, "flower_labels.txt");
            String str3 = "flower_model_180412_0";
            this.f110q = loadModel(context, str3);
            if (this.f110q != null) {
                Log.i(str2, "Load model \"flower_model_180412_0\" successfully.");
                this.f107n = new float[24300];
                this.f108o = new int[8100];
                this.f109p = new float[this.f106m.size()];
                StringBuilder sb = new StringBuilder();
                sb.append("deBouncing: ");
                sb.append(this.f102i);
                String sb2 = sb.toString();
                if (this.f102i) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(sb2);
                    sb3.append(", deBouncing-queue: 3");
                    sb2 = sb3.toString();
                }
                StringBuilder sb4 = new StringBuilder();
                sb4.append("model version: model_181113_0, threshold: 0.5, ");
                sb4.append(sb2);
                sb4.append(", flower-model version: ");
                sb4.append(str3);
                Log.i(str2, sb4.toString());
                return;
            }
            throw new RuntimeException(str);
        }
        throw new RuntimeException(str);
    }

    /* renamed from: a */
    private Bitmap m16a(Bitmap bitmap, int i) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == i && height == i) {
            return bitmap;
        }
        if (this.f94a.get(i) == null || ((BitmapPair) this.f94a.get(i)).f111a.getWidth() != i) {
            Bitmap createBitmap = Bitmap.createBitmap(i, i, Config.ARGB_8888);
            this.f94a.put(i, new BitmapPair(createBitmap, new Canvas(createBitmap)));
        }
        this.f97d.reset();
        float f = (float) i;
        this.f97d.postScale(f / ((float) width), f / ((float) height));
        ((BitmapPair) this.f94a.get(i)).f112b.drawBitmap(bitmap, this.f97d, null);
        return ((BitmapPair) this.f94a.get(i)).f111a;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0039, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        throw r3;
     */
    /* renamed from: a */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.List<java.lang.String> m17a(android.content.res.AssetManager r3, java.lang.String r4) {
        /*
            r2 = this;
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r0 = "Reading labels from: "
            r2.append(r0)
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            java.lang.String r0 = "SceneDetector"
            android.util.Log.i(r0, r2)
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.io.BufferedReader r0 = new java.io.BufferedReader     // Catch:{ IOException -> 0x0043 }
            java.io.InputStreamReader r1 = new java.io.InputStreamReader     // Catch:{ IOException -> 0x0043 }
            java.io.InputStream r3 = r3.open(r4)     // Catch:{ IOException -> 0x0043 }
            r1.<init>(r3)     // Catch:{ IOException -> 0x0043 }
            r0.<init>(r1)     // Catch:{ IOException -> 0x0043 }
        L_0x0029:
            java.lang.String r3 = r0.readLine()     // Catch:{ all -> 0x0037 }
            if (r3 == 0) goto L_0x0033
            r2.add(r3)     // Catch:{ all -> 0x0037 }
            goto L_0x0029
        L_0x0033:
            r0.close()     // Catch:{ IOException -> 0x0043 }
            return r2
        L_0x0037:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0039 }
        L_0x0039:
            r3 = move-exception
            r0.close()     // Catch:{ all -> 0x003e }
            goto L_0x0042
        L_0x003e:
            r4 = move-exception
            r2.addSuppressed(r4)     // Catch:{ IOException -> 0x0043 }
        L_0x0042:
            throw r3     // Catch:{ IOException -> 0x0043 }
        L_0x0043:
            r2 = move-exception
            java.lang.RuntimeException r3 = new java.lang.RuntimeException
            java.lang.String r4 = "Problem reading label file!"
            r3.<init>(r4, r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asus.scenedetectlib.detector.SceneDetector.m17a(android.content.res.AssetManager, java.lang.String):java.util.List");
    }

    /* renamed from: a */
    private void m18a(List<SceneRecognition> list) {
        SceneRecognition sceneRecognition;
        ArrayMap<String, Integer> arrayMap;
        Integer num;
        if (this.f103j == null) {
            this.f103j = new LimitedQueue<>(3);
        }
        if (this.f104k == null) {
            this.f104k = new ArrayMap<>();
        }
        SceneRecognition sceneRecognition2 = (SceneRecognition) list.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(sceneRecognition2.sceneType.name());
        String str = "SePaRaToR";
        sb.append(str);
        sb.append(sceneRecognition2.value);
        this.f103j.add(sb.toString());
        Iterator it = this.f103j.iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            if (!this.f104k.containsKey(str2)) {
                arrayMap = this.f104k;
                num = Integer.valueOf(1);
            } else {
                arrayMap = this.f104k;
                num = Integer.valueOf(((Integer) arrayMap.get(str2)).intValue() + 1);
            }
            arrayMap.put(str2, num);
        }
        int i = 0;
        for (int i2 = 0; i2 < this.f104k.size(); i2++) {
            if (((Integer) this.f104k.valueAt(i2)).intValue() > ((Integer) this.f104k.valueAt(i)).intValue()) {
                i = i2;
            }
        }
        if (((Integer) this.f104k.valueAt(i)).intValue() > 1) {
            String[] split = ((String) this.f104k.keyAt(i)).split(str);
            sceneRecognition = new SceneRecognition(SceneType.valueOf(split[0]), 1.0f);
            sceneRecognition.value = Integer.valueOf(split[1]).intValue();
        } else {
            sceneRecognition = new SceneRecognition(SceneType.OTHERS, 1.0f);
        }
        this.f104k.clear();
        int indexOf = list.indexOf(sceneRecognition);
        if (indexOf >= 0) {
            sceneRecognition = (SceneRecognition) list.remove(indexOf);
        }
        list.add(0, sceneRecognition);
    }

    private static native TensorFlowInferenceInterface loadModel(Context context, String str);

    /* JADX INFO: used method not loaded: org.tensorflow.contrib.android.TensorFlowInferenceInterface.feed(java.lang.String, float[], long[]):null, types can be incorrect */
    /* JADX INFO: used method not loaded: org.tensorflow.contrib.android.TensorFlowInferenceInterface.fetch(java.lang.String, float[]):null, types can be incorrect */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0207, code lost:
        r0 = new java.util.ArrayList(r3);
        r3.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x020f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008f, code lost:
        r3 = 0;
        r2 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0091, code lost:
        r5 = r1.f100g;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0094, code lost:
        if (r2 >= r5.length) goto L_0x00a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009c, code lost:
        if (r5[r2] <= r5[r3]) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009e, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009f, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a2, code lost:
        r2 = com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel((java.lang.String) r1.f95b.get(r3), r1.f100g[r3]);
        r3 = new java.util.ArrayList();
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b8, code lost:
        r8 = r1.f101h;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bb, code lost:
        if (r5 >= r8.length) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c3, code lost:
        if (r8[r5] <= 0.5f) goto L_0x00de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c5, code lost:
        r8 = com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel((java.lang.String) r1.f96c.get(r5), r1.f101h[r5]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00d9, code lost:
        if (r3.contains(r8) != false) goto L_0x00de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00db, code lost:
        r3.add(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00de, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e3, code lost:
        r5 = r3.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00eb, code lost:
        if (r5.hasNext() == false) goto L_0x0101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ed, code lost:
        r8 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r5.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f7, code lost:
        if (r2.sceneType != r8.sceneType) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f9, code lost:
        r2.confidence = r8.confidence;
        r5.remove();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0101, code lost:
        r3.add(0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0108, code lost:
        if (r2.sceneType != com.asus.scenedetectlib.AISceneDetectInterface.SceneType.FLOWER) goto L_0x01e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x010a, code lost:
        monitor-enter(r24);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010d, code lost:
        if (r1.f110q == null) goto L_0x01e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x010f, code lost:
        m16a(r0, 90).getPixels(r1.f108o, 0, 90, 0, 0, 90, 90);
        r0 = r1.f108o;
        r5 = r0.length;
        r8 = 0;
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x012d, code lost:
        if (r8 >= r5) goto L_0x0159;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x012f, code lost:
        r12 = r0[r8];
        r17 = r11 + 1;
        r1.f107n[r11] = ((float) ((r12 >> 16) & 255)) / 255.0f;
        r14 = r17 + 1;
        r1.f107n[r17] = ((float) ((r12 >> 8) & 255)) / 255.0f;
        r15 = r14 + 1;
        r1.f107n[r14] = ((float) (r12 & 255)) / 255.0f;
        r8 = r8 + 1;
        r11 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0159, code lost:
        r0 = r1.f110q;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x015b, code lost:
        r5 = "mInput";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r0.feed(r5, r1.f107n, 1, 90, 90, 3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r1.f110q.run(new java.lang.String[]{"mOutput/Softmax"}, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        r1.f110q.fetch("mOutput/Softmax", r1.f109p);
        r5 = 0;
        r0 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x018b, code lost:
        if (r0 >= r1.f109p.length) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0197, code lost:
        if (r1.f109p[r5] >= r1.f109p[r0]) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0199, code lost:
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x019a, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01ab, code lost:
        if (((java.lang.String) r1.f106m.get(r5)).equals("potplant") == false) goto L_0x01cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01ad, code lost:
        r0 = r3.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01b5, code lost:
        if (r0.hasNext() == false) goto L_0x01c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01b7, code lost:
        r4 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r0.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01c1, code lost:
        if (r4.sceneType != com.asus.scenedetectlib.AISceneDetectInterface.SceneType.PLANT) goto L_0x01b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01c3, code lost:
        r3.remove(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01c6, code lost:
        r2.sceneType = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.PLANT;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01cb, code lost:
        r3.set(0, com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel((java.lang.String) r1.f106m.get(r5), ((com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r3.get(0)).confidence));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01e2, code lost:
        monitor-exit(r24);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01e9, code lost:
        if (r1.f102i == false) goto L_0x0207;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01eb, code lost:
        m18a((java.util.List<com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition>) r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01f2, code lost:
        if (r13 >= r3.size()) goto L_0x0207;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01fe, code lost:
        if (((com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r3.get(r13)).sceneType != com.asus.scenedetectlib.AISceneDetectInterface.SceneType.OTHERS) goto L_0x0204;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0200, code lost:
        r3.remove(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0204, code lost:
        r13 = r13 + 1;
     */
    /* renamed from: a */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<com.asus.scenedetectlib.AISceneDetectInterface.recognition.Recognition> mo8225a(android.graphics.Bitmap r25) {
        /*
            r24 = this;
            r1 = r24
            r0 = r25
            monitor-enter(r24)
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r2 = r1.f105l     // Catch:{ all -> 0x0210 }
            if (r2 != 0) goto L_0x000c
            r0 = 0
            monitor-exit(r24)     // Catch:{ all -> 0x0210 }
            return r0
        L_0x000c:
            r2 = 224(0xe0, float:3.14E-43)
            android.graphics.Bitmap r3 = r1.m16a(r0, r2)     // Catch:{ all -> 0x0210 }
            int[] r4 = r1.f99f     // Catch:{ all -> 0x0210 }
            r5 = 0
            r6 = 224(0xe0, float:3.14E-43)
            r7 = 0
            r8 = 0
            r9 = 224(0xe0, float:3.14E-43)
            r10 = 224(0xe0, float:3.14E-43)
            r3.getPixels(r4, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0210 }
            int[] r2 = r1.f99f     // Catch:{ all -> 0x0210 }
            int r3 = r2.length     // Catch:{ all -> 0x0210 }
            r4 = 0
            r5 = r4
            r6 = r5
        L_0x0026:
            r7 = 1132396544(0x437f0000, float:255.0)
            if (r5 >= r3) goto L_0x0052
            r8 = r2[r5]     // Catch:{ all -> 0x0210 }
            float[] r9 = r1.f98e     // Catch:{ all -> 0x0210 }
            int r10 = r6 + 1
            int r11 = r8 >> 16
            r11 = r11 & 255(0xff, float:3.57E-43)
            float r11 = (float) r11     // Catch:{ all -> 0x0210 }
            float r11 = r11 / r7
            r9[r6] = r11     // Catch:{ all -> 0x0210 }
            float[] r6 = r1.f98e     // Catch:{ all -> 0x0210 }
            int r9 = r10 + 1
            int r11 = r8 >> 8
            r11 = r11 & 255(0xff, float:3.57E-43)
            float r11 = (float) r11     // Catch:{ all -> 0x0210 }
            float r11 = r11 / r7
            r6[r10] = r11     // Catch:{ all -> 0x0210 }
            float[] r6 = r1.f98e     // Catch:{ all -> 0x0210 }
            int r10 = r9 + 1
            r8 = r8 & 255(0xff, float:3.57E-43)
            float r8 = (float) r8     // Catch:{ all -> 0x0210 }
            float r8 = r8 / r7
            r6[r9] = r8     // Catch:{ all -> 0x0210 }
            int r5 = r5 + 1
            r6 = r10
            goto L_0x0026
        L_0x0052:
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r2 = r1.f105l     // Catch:{ all -> 0x0210 }
            java.lang.String r3 = "input_1"
            float[] r5 = r1.f98e     // Catch:{ all -> 0x0210 }
            r6 = 4
            long[] r8 = new long[r6]     // Catch:{ all -> 0x0210 }
            r9 = 1
            r8[r4] = r9     // Catch:{ all -> 0x0210 }
            r11 = 224(0xe0, double:1.107E-321)
            r13 = 1
            r8[r13] = r11     // Catch:{ all -> 0x0210 }
            r14 = 2
            r8[r14] = r11     // Catch:{ all -> 0x0210 }
            r11 = 3
            r15 = 3
            r8[r15] = r11     // Catch:{ all -> 0x0210 }
            r2.feed(r3, r5, r8)     // Catch:{ all -> 0x0210 }
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r2 = r1.f105l     // Catch:{ all -> 0x0210 }
            java.lang.String r3 = "model_2/single_output/Softmax"
            java.lang.String r5 = "model_3/multi_output/Sigmoid"
            java.lang.String[] r3 = new java.lang.String[]{r3, r5}     // Catch:{ all -> 0x0210 }
            r2.run(r3, r4)     // Catch:{ all -> 0x0210 }
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r2 = r1.f105l     // Catch:{ all -> 0x0210 }
            java.lang.String r3 = "model_2/single_output/Softmax"
            float[] r5 = r1.f100g     // Catch:{ all -> 0x0210 }
            r2.fetch(r3, r5)     // Catch:{ all -> 0x0210 }
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r2 = r1.f105l     // Catch:{ all -> 0x0210 }
            java.lang.String r3 = "model_3/multi_output/Sigmoid"
            float[] r5 = r1.f101h     // Catch:{ all -> 0x0210 }
            r2.fetch(r3, r5)     // Catch:{ all -> 0x0210 }
            monitor-exit(r24)     // Catch:{ all -> 0x0210 }
            r3 = r4
            r2 = r13
        L_0x0091:
            float[] r5 = r1.f100g
            int r8 = r5.length
            if (r2 >= r8) goto L_0x00a2
            r8 = r5[r2]
            r5 = r5[r3]
            int r5 = (r8 > r5 ? 1 : (r8 == r5 ? 0 : -1))
            if (r5 <= 0) goto L_0x009f
            r3 = r2
        L_0x009f:
            int r2 = r2 + 1
            goto L_0x0091
        L_0x00a2:
            java.util.List<java.lang.String> r2 = r1.f95b
            java.lang.Object r2 = r2.get(r3)
            java.lang.String r2 = (java.lang.String) r2
            float[] r5 = r1.f100g
            r3 = r5[r3]
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r2 = com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel(r2, r3)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r5 = r4
        L_0x00b8:
            float[] r8 = r1.f101h
            int r11 = r8.length
            if (r5 >= r11) goto L_0x00e3
            r8 = r8[r5]
            r11 = 1056964608(0x3f000000, float:0.5)
            int r8 = (r8 > r11 ? 1 : (r8 == r11 ? 0 : -1))
            if (r8 <= 0) goto L_0x00de
            java.util.List<java.lang.String> r8 = r1.f96c
            java.lang.Object r8 = r8.get(r5)
            java.lang.String r8 = (java.lang.String) r8
            float[] r11 = r1.f101h
            r11 = r11[r5]
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r8 = com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel(r8, r11)
            boolean r11 = r3.contains(r8)
            if (r11 != 0) goto L_0x00de
            r3.add(r8)
        L_0x00de:
            int r5 = r5 + 1
            r11 = 3
            goto L_0x00b8
        L_0x00e3:
            java.util.Iterator r5 = r3.iterator()
        L_0x00e7:
            boolean r8 = r5.hasNext()
            if (r8 == 0) goto L_0x0101
            java.lang.Object r8 = r5.next()
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r8 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r8
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r11 = r2.sceneType
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r12 = r8.sceneType
            if (r11 != r12) goto L_0x00e7
            float r8 = r8.confidence
            r2.confidence = r8
            r5.remove()
            goto L_0x00e7
        L_0x0101:
            r3.add(r4, r2)
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r5 = r2.sceneType
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r8 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.FLOWER
            if (r5 != r8) goto L_0x01e7
            monitor-enter(r24)
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r5 = r1.f110q     // Catch:{ all -> 0x01e4 }
            if (r5 == 0) goto L_0x01e2
            r5 = 90
            android.graphics.Bitmap r16 = r1.m16a(r0, r5)     // Catch:{ all -> 0x01e4 }
            int[] r0 = r1.f108o     // Catch:{ all -> 0x01e4 }
            r18 = 0
            r19 = 90
            r20 = 0
            r21 = 0
            r22 = 90
            r23 = 90
            r17 = r0
            r16.getPixels(r17, r18, r19, r20, r21, r22, r23)     // Catch:{ all -> 0x01e4 }
            int[] r0 = r1.f108o     // Catch:{ all -> 0x01e4 }
            int r5 = r0.length     // Catch:{ all -> 0x01e4 }
            r8 = r4
            r11 = r8
        L_0x012d:
            if (r8 >= r5) goto L_0x0159
            r12 = r0[r8]     // Catch:{ all -> 0x01e4 }
            float[] r15 = r1.f107n     // Catch:{ all -> 0x01e4 }
            int r17 = r11 + 1
            int r14 = r12 >> 16
            r14 = r14 & 255(0xff, float:3.57E-43)
            float r14 = (float) r14     // Catch:{ all -> 0x01e4 }
            float r14 = r14 / r7
            r15[r11] = r14     // Catch:{ all -> 0x01e4 }
            float[] r11 = r1.f107n     // Catch:{ all -> 0x01e4 }
            int r14 = r17 + 1
            int r15 = r12 >> 8
            r15 = r15 & 255(0xff, float:3.57E-43)
            float r15 = (float) r15     // Catch:{ all -> 0x01e4 }
            float r15 = r15 / r7
            r11[r17] = r15     // Catch:{ all -> 0x01e4 }
            float[] r11 = r1.f107n     // Catch:{ all -> 0x01e4 }
            int r15 = r14 + 1
            r12 = r12 & 255(0xff, float:3.57E-43)
            float r12 = (float) r12     // Catch:{ all -> 0x01e4 }
            float r12 = r12 / r7
            r11[r14] = r12     // Catch:{ all -> 0x01e4 }
            int r8 = r8 + 1
            r11 = r15
            r14 = 2
            r15 = 3
            goto L_0x012d
        L_0x0159:
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r0 = r1.f110q     // Catch:{ all -> 0x01e4 }
            java.lang.String r5 = "mInput"
            float[] r7 = r1.f107n     // Catch:{ all -> 0x01e4 }
            long[] r6 = new long[r6]     // Catch:{ all -> 0x01e4 }
            r6[r4] = r9     // Catch:{ all -> 0x01e4 }
            r8 = 90
            r6[r13] = r8     // Catch:{ all -> 0x01e4 }
            r10 = 2
            r6[r10] = r8     // Catch:{ all -> 0x01e4 }
            r8 = 3
            r10 = 3
            r6[r10] = r8     // Catch:{ all -> 0x01e4 }
            r0.feed(r5, r7, r6)     // Catch:{ all -> 0x01e4 }
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r0 = r1.f110q     // Catch:{ all -> 0x01e4 }
            java.lang.String r5 = "mOutput/Softmax"
            java.lang.String[] r5 = new java.lang.String[]{r5}     // Catch:{ all -> 0x01e4 }
            r0.run(r5, r4)     // Catch:{ all -> 0x01e4 }
            org.tensorflow.contrib.android.TensorFlowInferenceInterface r0 = r1.f110q     // Catch:{ all -> 0x01e4 }
            java.lang.String r5 = "mOutput/Softmax"
            float[] r6 = r1.f109p     // Catch:{ all -> 0x01e4 }
            r0.fetch(r5, r6)     // Catch:{ all -> 0x01e4 }
            r5 = r4
            r0 = r13
        L_0x0188:
            float[] r6 = r1.f109p     // Catch:{ all -> 0x01e4 }
            int r6 = r6.length     // Catch:{ all -> 0x01e4 }
            if (r0 >= r6) goto L_0x019d
            float[] r6 = r1.f109p     // Catch:{ all -> 0x01e4 }
            r6 = r6[r5]     // Catch:{ all -> 0x01e4 }
            float[] r7 = r1.f109p     // Catch:{ all -> 0x01e4 }
            r7 = r7[r0]     // Catch:{ all -> 0x01e4 }
            int r6 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1))
            if (r6 >= 0) goto L_0x019a
            r5 = r0
        L_0x019a:
            int r0 = r0 + 1
            goto L_0x0188
        L_0x019d:
            java.util.List<java.lang.String> r0 = r1.f106m     // Catch:{ all -> 0x01e4 }
            java.lang.Object r0 = r0.get(r5)     // Catch:{ all -> 0x01e4 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x01e4 }
            java.lang.String r6 = "potplant"
            boolean r0 = r0.equals(r6)     // Catch:{ all -> 0x01e4 }
            if (r0 == 0) goto L_0x01cb
            java.util.Iterator r0 = r3.iterator()     // Catch:{ all -> 0x01e4 }
        L_0x01b1:
            boolean r4 = r0.hasNext()     // Catch:{ all -> 0x01e4 }
            if (r4 == 0) goto L_0x01c6
            java.lang.Object r4 = r0.next()     // Catch:{ all -> 0x01e4 }
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r4 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r4     // Catch:{ all -> 0x01e4 }
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r5 = r4.sceneType     // Catch:{ all -> 0x01e4 }
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r6 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.PLANT     // Catch:{ all -> 0x01e4 }
            if (r5 != r6) goto L_0x01b1
            r3.remove(r4)     // Catch:{ all -> 0x01e4 }
        L_0x01c6:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r0 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.PLANT     // Catch:{ all -> 0x01e4 }
            r2.sceneType = r0     // Catch:{ all -> 0x01e4 }
            goto L_0x01e2
        L_0x01cb:
            java.util.List<java.lang.String> r0 = r1.f106m     // Catch:{ all -> 0x01e4 }
            java.lang.Object r0 = r0.get(r5)     // Catch:{ all -> 0x01e4 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x01e4 }
            java.lang.Object r2 = r3.get(r4)     // Catch:{ all -> 0x01e4 }
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r2 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r2     // Catch:{ all -> 0x01e4 }
            float r2 = r2.confidence     // Catch:{ all -> 0x01e4 }
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r0 = com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.fromLabel(r0, r2)     // Catch:{ all -> 0x01e4 }
            r3.set(r4, r0)     // Catch:{ all -> 0x01e4 }
        L_0x01e2:
            monitor-exit(r24)     // Catch:{ all -> 0x01e4 }
            goto L_0x01e7
        L_0x01e4:
            r0 = move-exception
            monitor-exit(r24)     // Catch:{ all -> 0x01e4 }
            throw r0
        L_0x01e7:
            boolean r0 = r1.f102i
            if (r0 == 0) goto L_0x0207
            r1.m18a(r3)
        L_0x01ee:
            int r0 = r3.size()
            if (r13 >= r0) goto L_0x0207
            java.lang.Object r0 = r3.get(r13)
            com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition r0 = (com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition) r0
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r0 = r0.sceneType
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.OTHERS
            if (r0 != r1) goto L_0x0204
            r3.remove(r13)
            goto L_0x0207
        L_0x0204:
            int r13 = r13 + 1
            goto L_0x01ee
        L_0x0207:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>(r3)
            r3.clear()
            return r0
        L_0x0210:
            r0 = move-exception
            monitor-exit(r24)     // Catch:{ all -> 0x0210 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asus.scenedetectlib.detector.SceneDetector.mo8225a(android.graphics.Bitmap):java.util.List");
    }

    public void release() {
        super.release();
        synchronized (this) {
            if (this.f105l != null) {
                this.f105l.close();
                this.f105l = null;
            }
            if (this.f110q != null) {
                this.f110q.close();
                this.f110q = null;
            }
            this.f94a.clear();
            if (this.f103j != null) {
                this.f103j.clear();
                this.f103j = null;
            }
            if (this.f104k != null) {
                this.f104k.clear();
                this.f104k = null;
            }
        }
    }
}
