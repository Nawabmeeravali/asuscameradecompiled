package org.tensorflow;

import com.asus.scenedetectlib.BuildConfig;
import java.util.Iterator;

public final class Graph implements AutoCloseable {
    /* access modifiers changed from: private */
    public long nativeHandle;
    /* access modifiers changed from: private */
    public final Object nativeHandleLock;
    /* access modifiers changed from: private */
    public int refcount;

    private static final class OperationIterator implements Iterator<Operation> {
        private final Graph graph;
        private Operation operation = null;
        private int position = 0;

        OperationIterator(Graph graph2) {
            this.graph = graph2;
            advance();
        }

        private final void advance() {
            Reference ref = this.graph.ref();
            this.operation = null;
            try {
                long[] access$400 = Graph.nextOperation(ref.nativeHandle(), this.position);
                if (!(access$400 == null || access$400[0] == 0)) {
                    this.operation = new Operation(this.graph, access$400[0]);
                    this.position = (int) access$400[1];
                }
            } finally {
                ref.close();
            }
        }

        public boolean hasNext() {
            return this.operation != null;
        }

        public Operation next() {
            Operation operation2 = this.operation;
            advance();
            return operation2;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() is unsupported.");
        }
    }

    class Reference implements AutoCloseable {
        private boolean active;

        private Reference() {
            synchronized (Graph.this.nativeHandleLock) {
                this.active = Graph.this.nativeHandle != 0;
                if (this.active) {
                    this.active = true;
                    Graph.this.refcount = Graph.this.refcount + 1;
                } else {
                    throw new IllegalStateException("close() has been called on the Graph");
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() {
            /*
                r2 = this;
                org.tensorflow.Graph r0 = org.tensorflow.Graph.this
                java.lang.Object r0 = r0.nativeHandleLock
                monitor-enter(r0)
                boolean r1 = r2.active     // Catch:{ all -> 0x0023 }
                if (r1 != 0) goto L_0x000d
                monitor-exit(r0)     // Catch:{ all -> 0x0023 }
                return
            L_0x000d:
                r1 = 0
                r2.active = r1     // Catch:{ all -> 0x0023 }
                org.tensorflow.Graph r1 = org.tensorflow.Graph.this     // Catch:{ all -> 0x0023 }
                int r1 = org.tensorflow.Graph.access$206(r1)     // Catch:{ all -> 0x0023 }
                if (r1 != 0) goto L_0x0021
                org.tensorflow.Graph r2 = org.tensorflow.Graph.this     // Catch:{ all -> 0x0023 }
                java.lang.Object r2 = r2.nativeHandleLock     // Catch:{ all -> 0x0023 }
                r2.notifyAll()     // Catch:{ all -> 0x0023 }
            L_0x0021:
                monitor-exit(r0)     // Catch:{ all -> 0x0023 }
                return
            L_0x0023:
                r2 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0023 }
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Graph.Reference.close():void");
        }

        public long nativeHandle() {
            long access$100;
            synchronized (Graph.this.nativeHandleLock) {
                access$100 = this.active ? Graph.this.nativeHandle : 0;
            }
            return access$100;
        }
    }

    private static native long allocate();

    private static native void delete(long j);

    private static native void importGraphDef(long j, byte[] bArr, String str) throws IllegalArgumentException;

    /* access modifiers changed from: private */
    public static native long[] nextOperation(long j, int i);

    private static native long operation(long j, String str);

    private static native byte[] toGraphDef(long j);

    static /* synthetic */ int access$206(Graph graph) {
        int i = graph.refcount - 1;
        graph.refcount = i;
        return i;
    }

    public Graph() {
        this.nativeHandleLock = new Object();
        this.refcount = 0;
        this.nativeHandle = allocate();
    }

    Graph(long j) {
        this.nativeHandleLock = new Object();
        this.refcount = 0;
        this.nativeHandle = j;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:9|10|11|12|13|14|7) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0017 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r5 = this;
            java.lang.Object r0 = r5.nativeHandleLock
            monitor-enter(r0)
            long r1 = r5.nativeHandle     // Catch:{ all -> 0x0029 }
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 != 0) goto L_0x000d
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x000d:
            int r1 = r5.refcount     // Catch:{ all -> 0x0029 }
            if (r1 <= 0) goto L_0x0020
            java.lang.Object r1 = r5.nativeHandleLock     // Catch:{ InterruptedException -> 0x0017 }
            r1.wait()     // Catch:{ InterruptedException -> 0x0017 }
            goto L_0x000d
        L_0x0017:
            java.lang.Thread r5 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0029 }
            r5.interrupt()     // Catch:{ all -> 0x0029 }
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0020:
            long r1 = r5.nativeHandle     // Catch:{ all -> 0x0029 }
            delete(r1)     // Catch:{ all -> 0x0029 }
            r5.nativeHandle = r3     // Catch:{ all -> 0x0029 }
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0029:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.Graph.close():void");
    }

    public Operation operation(String str) {
        synchronized (this.nativeHandleLock) {
            long operation = operation(this.nativeHandle, str);
            if (operation == 0) {
                return null;
            }
            Operation operation2 = new Operation(this, operation);
            return operation2;
        }
    }

    public Iterator<Operation> operations() {
        return new OperationIterator(this);
    }

    public OperationBuilder opBuilder(String str, String str2) {
        return new OperationBuilder(this, str, str2);
    }

    public void importGraphDef(byte[] bArr) throws IllegalArgumentException {
        importGraphDef(bArr, BuildConfig.FLAVOR);
    }

    public void importGraphDef(byte[] bArr, String str) throws IllegalArgumentException {
        if (bArr == null || str == null) {
            throw new IllegalArgumentException("graphDef and prefix cannot be null");
        }
        synchronized (this.nativeHandleLock) {
            importGraphDef(this.nativeHandle, bArr, str);
        }
    }

    public byte[] toGraphDef() {
        byte[] graphDef;
        synchronized (this.nativeHandleLock) {
            graphDef = toGraphDef(this.nativeHandle);
        }
        return graphDef;
    }

    /* access modifiers changed from: 0000 */
    public Reference ref() {
        return new Reference();
    }

    static {
        TensorFlow.init();
    }
}
