package org.tensorflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Session implements AutoCloseable {
    /* access modifiers changed from: private */
    public final Graph graph;
    private final Reference graphRef;
    /* access modifiers changed from: private */
    public long nativeHandle;
    /* access modifiers changed from: private */
    public final Object nativeHandleLock;
    private int numActiveRuns;

    public static final class Run {
        public byte[] metadata;
        public List<Tensor<?>> outputs;
    }

    public final class Runner {
        private ArrayList<Tensor<?>> inputTensors = new ArrayList<>();
        private ArrayList<Output<?>> inputs = new ArrayList<>();
        private ArrayList<Output<?>> outputs = new ArrayList<>();
        private byte[] runOptions = null;
        private ArrayList<Operation> targets = new ArrayList<>();

        private class Reference implements AutoCloseable {
            public Reference() {
                synchronized (Session.this.nativeHandleLock) {
                    if (Session.this.nativeHandle != 0) {
                        Session.access$304(Session.this);
                    } else {
                        throw new IllegalStateException("run() cannot be called on the Session after close()");
                    }
                }
            }

            /* JADX WARNING: Code restructure failed: missing block: B:11:0x002f, code lost:
                return;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void close() {
                /*
                    r5 = this;
                    org.tensorflow.Session$Runner r0 = org.tensorflow.Session.Runner.this
                    org.tensorflow.Session r0 = org.tensorflow.Session.this
                    java.lang.Object r0 = r0.nativeHandleLock
                    monitor-enter(r0)
                    org.tensorflow.Session$Runner r1 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r1 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    long r1 = r1.nativeHandle     // Catch:{ all -> 0x0030 }
                    r3 = 0
                    int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
                    if (r1 != 0) goto L_0x0019
                    monitor-exit(r0)     // Catch:{ all -> 0x0030 }
                    return
                L_0x0019:
                    org.tensorflow.Session$Runner r1 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r1 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    int r1 = org.tensorflow.Session.access$306(r1)     // Catch:{ all -> 0x0030 }
                    if (r1 != 0) goto L_0x002e
                    org.tensorflow.Session$Runner r5 = org.tensorflow.Session.Runner.this     // Catch:{ all -> 0x0030 }
                    org.tensorflow.Session r5 = org.tensorflow.Session.this     // Catch:{ all -> 0x0030 }
                    java.lang.Object r5 = r5.nativeHandleLock     // Catch:{ all -> 0x0030 }
                    r5.notifyAll()     // Catch:{ all -> 0x0030 }
                L_0x002e:
                    monitor-exit(r0)     // Catch:{ all -> 0x0030 }
                    return
                L_0x0030:
                    r5 = move-exception
                    monitor-exit(r0)     // Catch:{ all -> 0x0030 }
                    throw r5
                */
                throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Session.Runner.Reference.close():void");
            }
        }

        public Runner() {
        }

        public Runner feed(String str, Tensor<?> tensor) {
            return feed(parseOutput(str), tensor);
        }

        public Runner feed(String str, int i, Tensor<?> tensor) {
            Operation operationByName = operationByName(str);
            if (operationByName != null) {
                this.inputs.add(operationByName.output(i));
                this.inputTensors.add(tensor);
            }
            return this;
        }

        public Runner feed(Output<?> output, Tensor<?> tensor) {
            this.inputs.add(output);
            this.inputTensors.add(tensor);
            return this;
        }

        public Runner fetch(String str) {
            return fetch(parseOutput(str));
        }

        public Runner fetch(String str, int i) {
            Operation operationByName = operationByName(str);
            if (operationByName != null) {
                this.outputs.add(operationByName.output(i));
            }
            return this;
        }

        public Runner fetch(Output<?> output) {
            this.outputs.add(output);
            return this;
        }

        public Runner addTarget(String str) {
            Operation operationByName = operationByName(str);
            if (operationByName != null) {
                this.targets.add(operationByName);
            }
            return this;
        }

        public Runner addTarget(Operation operation) {
            this.targets.add(operation);
            return this;
        }

        public Runner setOptions(byte[] bArr) {
            this.runOptions = bArr;
            return this;
        }

        public List<Tensor<?>> run() {
            return runHelper(false).outputs;
        }

        public Run runAndFetchMetadata() {
            return runHelper(true);
        }

        /* JADX INFO: finally extract failed */
        private Run runHelper(boolean z) {
            long[] jArr = new long[this.inputTensors.size()];
            long[] jArr2 = new long[this.inputs.size()];
            int[] iArr = new int[this.inputs.size()];
            long[] jArr3 = new long[this.outputs.size()];
            int[] iArr2 = new int[this.outputs.size()];
            long[] jArr4 = new long[this.targets.size()];
            long[] jArr5 = new long[this.outputs.size()];
            Iterator it = this.inputTensors.iterator();
            int i = 0;
            int i2 = 0;
            while (it.hasNext()) {
                int i3 = i2 + 1;
                jArr[i2] = ((Tensor) it.next()).getNativeHandle();
                i2 = i3;
            }
            Iterator it2 = this.inputs.iterator();
            int i4 = 0;
            while (it2.hasNext()) {
                Output output = (Output) it2.next();
                jArr2[i4] = output.mo8356op().getUnsafeNativeHandle();
                iArr[i4] = output.index();
                i4++;
            }
            Iterator it3 = this.outputs.iterator();
            int i5 = 0;
            while (it3.hasNext()) {
                Output output2 = (Output) it3.next();
                jArr3[i5] = output2.mo8356op().getUnsafeNativeHandle();
                iArr2[i5] = output2.index();
                i5++;
            }
            Iterator it4 = this.targets.iterator();
            int i6 = 0;
            while (it4.hasNext()) {
                int i7 = i6 + 1;
                jArr4[i6] = ((Operation) it4.next()).getUnsafeNativeHandle();
                i6 = i7;
            }
            Reference reference = new Reference();
            try {
                byte[] access$100 = Session.run(Session.this.nativeHandle, this.runOptions, jArr, jArr2, iArr, jArr3, iArr2, jArr4, z, jArr5);
                reference.close();
                ArrayList<Tensor> arrayList = new ArrayList<>();
                int length = jArr5.length;
                while (i < length) {
                    try {
                        arrayList.add(Tensor.fromHandle(jArr5[i]));
                        i++;
                    } catch (Exception e) {
                        for (Tensor close : arrayList) {
                            close.close();
                        }
                        arrayList.clear();
                        throw e;
                    }
                }
                Run run = new Run();
                run.outputs = arrayList;
                run.metadata = access$100;
                return run;
            } catch (Throwable th) {
                reference.close();
                throw th;
            }
        }

        private Operation operationByName(String str) {
            Operation operation = Session.this.graph.operation(str);
            if (operation != null) {
                return operation;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("No Operation named [");
            sb.append(str);
            sb.append("] in the Graph");
            throw new IllegalArgumentException(sb.toString());
        }

        private Output<?> parseOutput(String str) {
            int lastIndexOf = str.lastIndexOf(58);
            if (lastIndexOf == -1 || lastIndexOf == str.length() - 1) {
                return new Output<>(operationByName(str), 0);
            }
            try {
                String substring = str.substring(0, lastIndexOf);
                return new Output<>(operationByName(substring), Integer.parseInt(str.substring(lastIndexOf + 1)));
            } catch (NumberFormatException unused) {
                return new Output<>(operationByName(str), 0);
            }
        }
    }

    private static native long allocate(long j);

    private static native long allocate2(long j, String str, byte[] bArr);

    private static native void delete(long j);

    /* access modifiers changed from: private */
    public static native byte[] run(long j, byte[] bArr, long[] jArr, long[] jArr2, int[] iArr, long[] jArr3, int[] iArr2, long[] jArr4, boolean z, long[] jArr5);

    static /* synthetic */ int access$304(Session session) {
        int i = session.numActiveRuns + 1;
        session.numActiveRuns = i;
        return i;
    }

    static /* synthetic */ int access$306(Session session) {
        int i = session.numActiveRuns - 1;
        session.numActiveRuns = i;
        return i;
    }

    public Session(Graph graph2) {
        this(graph2, (byte[]) null);
    }

    public Session(Graph graph2, byte[] bArr) {
        long j;
        this.nativeHandleLock = new Object();
        this.graph = graph2;
        Reference ref = graph2.ref();
        if (bArr == null) {
            try {
                j = allocate(ref.nativeHandle());
            } catch (Throwable th) {
                ref.close();
                throw th;
            }
        } else {
            j = allocate2(ref.nativeHandle(), null, bArr);
        }
        this.nativeHandle = j;
        this.graphRef = graph2.ref();
        ref.close();
    }

    Session(Graph graph2, long j) {
        this.nativeHandleLock = new Object();
        this.graph = graph2;
        this.nativeHandle = j;
        this.graphRef = graph2.ref();
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:9|10|11|12|13|14|7) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x001c */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r5 = this;
            org.tensorflow.Graph$Reference r0 = r5.graphRef
            r0.close()
            java.lang.Object r0 = r5.nativeHandleLock
            monitor-enter(r0)
            long r1 = r5.nativeHandle     // Catch:{ all -> 0x002e }
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 != 0) goto L_0x0012
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x0012:
            int r1 = r5.numActiveRuns     // Catch:{ all -> 0x002e }
            if (r1 <= 0) goto L_0x0025
            java.lang.Object r1 = r5.nativeHandleLock     // Catch:{ InterruptedException -> 0x001c }
            r1.wait()     // Catch:{ InterruptedException -> 0x001c }
            goto L_0x0012
        L_0x001c:
            java.lang.Thread r5 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x002e }
            r5.interrupt()     // Catch:{ all -> 0x002e }
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x0025:
            long r1 = r5.nativeHandle     // Catch:{ all -> 0x002e }
            delete(r1)     // Catch:{ all -> 0x002e }
            r5.nativeHandle = r3     // Catch:{ all -> 0x002e }
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return
        L_0x002e:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Session.close():void");
    }

    public Runner runner() {
        return new Runner();
    }
}
