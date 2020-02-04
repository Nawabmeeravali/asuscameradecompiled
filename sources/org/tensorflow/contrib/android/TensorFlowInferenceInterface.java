package org.tensorflow.contrib.android;

import android.content.res.AssetManager;
import android.os.Build.VERSION;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import com.asus.scenedetectlib.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.Session.Run;
import org.tensorflow.Session.Runner;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;
import org.tensorflow.types.UInt8;

public class TensorFlowInferenceInterface {
    private static final String ASSET_FILE_PREFIX = "file:///android_asset/";
    private static final String TAG = "TensorFlowInferenceInterface";
    private List<String> feedNames = new ArrayList();
    private List<Tensor<?>> feedTensors = new ArrayList();
    private List<String> fetchNames = new ArrayList();
    private List<Tensor<?>> fetchTensors = new ArrayList();

    /* renamed from: g */
    private final Graph f119g;
    private final String modelName;
    private RunStats runStats;
    private Runner runner;
    private final Session sess;

    private static class TensorId {
        String name;
        int outputIndex;

        private TensorId() {
        }

        public static TensorId parse(String str) {
            TensorId tensorId = new TensorId();
            int lastIndexOf = str.lastIndexOf(58);
            if (lastIndexOf < 0) {
                tensorId.outputIndex = 0;
                tensorId.name = str;
                return tensorId;
            }
            try {
                tensorId.outputIndex = Integer.parseInt(str.substring(lastIndexOf + 1));
                tensorId.name = str.substring(0, lastIndexOf);
            } catch (NumberFormatException unused) {
                tensorId.outputIndex = 0;
                tensorId.name = str;
            }
            return tensorId;
        }
    }

    public TensorFlowInferenceInterface(AssetManager assetManager, String str) {
        InputStream inputStream;
        String str2;
        prepareNativeRuntime();
        this.modelName = str;
        this.f119g = new Graph();
        this.sess = new Session(this.f119g);
        this.runner = this.sess.runner();
        String str3 = ASSET_FILE_PREFIX;
        boolean startsWith = str.startsWith(str3);
        String str4 = "Failed to load model from '";
        String str5 = "'";
        if (startsWith) {
            try {
                str2 = str.split(str3)[1];
            } catch (IOException e) {
                if (!startsWith) {
                    try {
                        inputStream = new FileInputStream(str);
                    } catch (IOException unused) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(str4);
                        sb.append(str);
                        sb.append(str5);
                        throw new RuntimeException(sb.toString(), e);
                    }
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(str4);
                    sb2.append(str);
                    sb2.append(str5);
                    throw new RuntimeException(sb2.toString(), e);
                }
            }
        } else {
            str2 = str;
        }
        inputStream = assetManager.open(str2);
        try {
            if (VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }
            byte[] bArr = new byte[inputStream.available()];
            int read = inputStream.read(bArr);
            if (read == bArr.length) {
                if (VERSION.SDK_INT >= 18) {
                    Trace.endSection();
                }
                loadGraph(bArr, this.f119g);
                inputStream.close();
                String str6 = TAG;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Successfully loaded model from '");
                sb3.append(str);
                sb3.append(str5);
                Log.i(str6, sb3.toString());
                if (VERSION.SDK_INT >= 18) {
                    Trace.endSection();
                    return;
                }
                return;
            }
            StringBuilder sb4 = new StringBuilder();
            sb4.append("read error: read only ");
            sb4.append(read);
            sb4.append(" of the graph, expected to read ");
            sb4.append(bArr.length);
            throw new IOException(sb4.toString());
        } catch (IOException e2) {
            StringBuilder sb5 = new StringBuilder();
            sb5.append(str4);
            sb5.append(str);
            sb5.append(str5);
            throw new RuntimeException(sb5.toString(), e2);
        }
    }

    public TensorFlowInferenceInterface(InputStream inputStream) {
        prepareNativeRuntime();
        this.modelName = BuildConfig.FLAVOR;
        this.f119g = new Graph();
        this.sess = new Session(this.f119g);
        this.runner = this.sess.runner();
        try {
            if (VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(inputStream.available() > 16384 ? inputStream.available() : 16384);
            byte[] bArr = new byte[16384];
            while (true) {
                int read = inputStream.read(bArr, 0, bArr.length);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
            loadGraph(byteArray, this.f119g);
            Log.i(TAG, "Successfully loaded model from the input stream");
            if (VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model from the input stream", e);
        }
    }

    public TensorFlowInferenceInterface(Graph graph) {
        prepareNativeRuntime();
        this.modelName = BuildConfig.FLAVOR;
        this.f119g = graph;
        this.sess = new Session(graph);
        this.runner = this.sess.runner();
    }

    public void run(String[] strArr) {
        run(strArr, false);
    }

    public void run(String[] strArr, boolean z) {
        run(strArr, z, new String[0]);
    }

    public void run(String[] strArr, boolean z, String[] strArr2) {
        String str = ", ";
        closeFetches();
        for (String str2 : strArr) {
            this.fetchNames.add(str2);
            TensorId parse = TensorId.parse(str2);
            this.runner.fetch(parse.name, parse.outputIndex);
        }
        for (String addTarget : strArr2) {
            this.runner.addTarget(addTarget);
        }
        if (z) {
            try {
                Run runAndFetchMetadata = this.runner.setOptions(RunStats.runOptions()).runAndFetchMetadata();
                this.fetchTensors = runAndFetchMetadata.outputs;
                if (this.runStats == null) {
                    this.runStats = new RunStats();
                }
                this.runStats.add(runAndFetchMetadata.metadata);
            } catch (RuntimeException e) {
                String str3 = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to run TensorFlow inference with inputs:[");
                sb.append(TextUtils.join(str, this.feedNames));
                sb.append("], outputs:[");
                sb.append(TextUtils.join(str, this.fetchNames));
                sb.append("]");
                Log.e(str3, sb.toString());
                throw e;
            } catch (Throwable th) {
                closeFeeds();
                this.runner = this.sess.runner();
                throw th;
            }
        } else {
            this.fetchTensors = this.runner.run();
        }
        closeFeeds();
        this.runner = this.sess.runner();
    }

    public Graph graph() {
        return this.f119g;
    }

    public Operation graphOperation(String str) {
        Operation operation = this.f119g.operation(str);
        if (operation != null) {
            return operation;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Node '");
        sb.append(str);
        sb.append("' does not exist in model '");
        sb.append(this.modelName);
        sb.append("'");
        throw new RuntimeException(sb.toString());
    }

    public String getStatString() {
        RunStats runStats2 = this.runStats;
        return runStats2 == null ? BuildConfig.FLAVOR : runStats2.summary();
    }

    public void close() {
        closeFeeds();
        closeFetches();
        this.sess.close();
        this.f119g.close();
        RunStats runStats2 = this.runStats;
        if (runStats2 != null) {
            runStats2.close();
        }
        this.runStats = null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public void feed(String str, boolean[] zArr, long... jArr) {
        byte[] bArr = new byte[zArr.length];
        for (int i = 0; i < zArr.length; i++) {
            bArr[i] = zArr[i] ? (byte) 1 : 0;
        }
        addFeed(str, Tensor.create(Boolean.class, jArr, ByteBuffer.wrap(bArr)));
    }

    public void feed(String str, float[] fArr, long... jArr) {
        addFeed(str, Tensor.create(jArr, FloatBuffer.wrap(fArr)));
    }

    public void feed(String str, int[] iArr, long... jArr) {
        addFeed(str, Tensor.create(jArr, IntBuffer.wrap(iArr)));
    }

    public void feed(String str, long[] jArr, long... jArr2) {
        addFeed(str, Tensor.create(jArr2, LongBuffer.wrap(jArr)));
    }

    public void feed(String str, double[] dArr, long... jArr) {
        addFeed(str, Tensor.create(jArr, DoubleBuffer.wrap(dArr)));
    }

    public void feed(String str, byte[] bArr, long... jArr) {
        addFeed(str, Tensor.create(UInt8.class, jArr, ByteBuffer.wrap(bArr)));
    }

    public void feedString(String str, byte[] bArr) {
        addFeed(str, Tensors.create(bArr));
    }

    public void feedString(String str, byte[][] bArr) {
        addFeed(str, Tensors.create(bArr));
    }

    public void feed(String str, FloatBuffer floatBuffer, long... jArr) {
        addFeed(str, Tensor.create(jArr, floatBuffer));
    }

    public void feed(String str, IntBuffer intBuffer, long... jArr) {
        addFeed(str, Tensor.create(jArr, intBuffer));
    }

    public void feed(String str, LongBuffer longBuffer, long... jArr) {
        addFeed(str, Tensor.create(jArr, longBuffer));
    }

    public void feed(String str, DoubleBuffer doubleBuffer, long... jArr) {
        addFeed(str, Tensor.create(jArr, doubleBuffer));
    }

    public void feed(String str, ByteBuffer byteBuffer, long... jArr) {
        addFeed(str, Tensor.create(UInt8.class, jArr, byteBuffer));
    }

    public void fetch(String str, float[] fArr) {
        fetch(str, FloatBuffer.wrap(fArr));
    }

    public void fetch(String str, int[] iArr) {
        fetch(str, IntBuffer.wrap(iArr));
    }

    public void fetch(String str, long[] jArr) {
        fetch(str, LongBuffer.wrap(jArr));
    }

    public void fetch(String str, double[] dArr) {
        fetch(str, DoubleBuffer.wrap(dArr));
    }

    public void fetch(String str, byte[] bArr) {
        fetch(str, ByteBuffer.wrap(bArr));
    }

    public void fetch(String str, FloatBuffer floatBuffer) {
        getTensor(str).writeTo(floatBuffer);
    }

    public void fetch(String str, IntBuffer intBuffer) {
        getTensor(str).writeTo(intBuffer);
    }

    public void fetch(String str, LongBuffer longBuffer) {
        getTensor(str).writeTo(longBuffer);
    }

    public void fetch(String str, DoubleBuffer doubleBuffer) {
        getTensor(str).writeTo(doubleBuffer);
    }

    public void fetch(String str, ByteBuffer byteBuffer) {
        getTensor(str).writeTo(byteBuffer);
    }

    private void prepareNativeRuntime() {
        String str = TAG;
        Log.i(str, "Checking to see if TensorFlow native methods are already loaded");
        try {
            new RunStats();
            Log.i(str, "TensorFlow native methods already loaded");
        } catch (UnsatisfiedLinkError unused) {
            Log.i(str, "TensorFlow native methods not found, attempting to load via tensorflow_inference");
            try {
                System.loadLibrary("tensorflow_inference");
                Log.i(str, "Successfully loaded TensorFlow native methods (RunStats error may be ignored)");
            } catch (UnsatisfiedLinkError unused2) {
                throw new RuntimeException("Native TF methods not found; check that the correct native libraries are present in the APK.");
            }
        }
    }

    private void loadGraph(byte[] bArr, Graph graph) throws IOException {
        long currentTimeMillis = System.currentTimeMillis();
        if (VERSION.SDK_INT >= 18) {
            Trace.beginSection("importGraphDef");
        }
        try {
            graph.importGraphDef(bArr);
            if (VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
            long currentTimeMillis2 = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("Model load took ");
            sb.append(currentTimeMillis2 - currentTimeMillis);
            sb.append("ms, TensorFlow version: ");
            sb.append(TensorFlow.version());
            Log.i(TAG, sb.toString());
        } catch (IllegalArgumentException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Not a valid TensorFlow Graph serialization: ");
            sb2.append(e.getMessage());
            throw new IOException(sb2.toString());
        }
    }

    private void addFeed(String str, Tensor<?> tensor) {
        TensorId parse = TensorId.parse(str);
        this.runner.feed(parse.name, parse.outputIndex, tensor);
        this.feedNames.add(str);
        this.feedTensors.add(tensor);
    }

    private Tensor<?> getTensor(String str) {
        int i = 0;
        for (String equals : this.fetchNames) {
            if (equals.equals(str)) {
                return (Tensor) this.fetchTensors.get(i);
            }
            i++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Node '");
        sb.append(str);
        sb.append("' was not provided to run(), so it cannot be read");
        throw new RuntimeException(sb.toString());
    }

    private void closeFeeds() {
        for (Tensor close : this.feedTensors) {
            close.close();
        }
        this.feedTensors.clear();
        this.feedNames.clear();
    }

    private void closeFetches() {
        for (Tensor close : this.fetchTensors) {
            close.close();
        }
        this.fetchTensors.clear();
        this.fetchNames.clear();
    }
}
