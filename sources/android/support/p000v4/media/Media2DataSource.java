package android.support.p000v4.media;

import java.io.Closeable;
import java.io.IOException;

/* renamed from: android.support.v4.media.Media2DataSource */
public abstract class Media2DataSource implements Closeable {
    public abstract long getSize() throws IOException;

    public abstract int readAt(long j, byte[] bArr, int i, int i2) throws IOException;
}
