package org.tensorflow;

public class SavedModelBundle implements AutoCloseable {
    private final Graph graph;
    private final byte[] metaGraphDef;
    private final Session session;

    private static native SavedModelBundle load(String str, String[] strArr, byte[] bArr);

    public static SavedModelBundle load(String str, String... strArr) {
        return load(str, strArr, null);
    }

    public byte[] metaGraphDef() {
        return this.metaGraphDef;
    }

    public Graph graph() {
        return this.graph;
    }

    public Session session() {
        return this.session;
    }

    public void close() {
        this.session.close();
        this.graph.close();
    }

    private SavedModelBundle(Graph graph2, Session session2, byte[] bArr) {
        this.graph = graph2;
        this.session = session2;
        this.metaGraphDef = bArr;
    }

    private static SavedModelBundle fromHandle(long j, long j2, byte[] bArr) {
        Graph graph2 = new Graph(j);
        return new SavedModelBundle(graph2, new Session(graph2, j2), bArr);
    }

    static {
        TensorFlow.init();
    }
}
