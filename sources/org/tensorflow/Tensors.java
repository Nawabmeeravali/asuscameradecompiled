package org.tensorflow;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Tensors {
    private Tensors() {
    }

    public static Tensor<String> create(String str) {
        return Tensor.create((Object) str.getBytes(StandardCharsets.UTF_8), String.class);
    }

    public static Tensor<String> create(String str, Charset charset) {
        return Tensor.create((Object) str.getBytes(charset), String.class);
    }

    public static Tensor<Float> create(float f) {
        return Tensor.create((Object) Float.valueOf(f), Float.class);
    }

    public static Tensor<Float> create(float[] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Float> create(float[][] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Float> create(float[][][] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Float> create(float[][][][] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Float> create(float[][][][][] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Float> create(float[][][][][][] fArr) {
        return Tensor.create((Object) fArr, Float.class);
    }

    public static Tensor<Double> create(double d) {
        return Tensor.create((Object) Double.valueOf(d), Double.class);
    }

    public static Tensor<Double> create(double[] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Double> create(double[][] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Double> create(double[][][] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Double> create(double[][][][] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Double> create(double[][][][][] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Double> create(double[][][][][][] dArr) {
        return Tensor.create((Object) dArr, Double.class);
    }

    public static Tensor<Integer> create(int i) {
        return Tensor.create((Object) Integer.valueOf(i), Integer.class);
    }

    public static Tensor<Integer> create(int[] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<Integer> create(int[][] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<Integer> create(int[][][] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][][] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<Integer> create(int[][][][][][] iArr) {
        return Tensor.create((Object) iArr, Integer.class);
    }

    public static Tensor<String> create(byte[] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<String> create(byte[][] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<String> create(byte[][][] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<String> create(byte[][][][] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<String> create(byte[][][][][] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<String> create(byte[][][][][][] bArr) {
        return Tensor.create((Object) bArr, String.class);
    }

    public static Tensor<Long> create(long j) {
        return Tensor.create((Object) Long.valueOf(j), Long.class);
    }

    public static Tensor<Long> create(long[] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Long> create(long[][] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Long> create(long[][][] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Long> create(long[][][][] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Long> create(long[][][][][] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Long> create(long[][][][][][] jArr) {
        return Tensor.create((Object) jArr, Long.class);
    }

    public static Tensor<Boolean> create(boolean z) {
        return Tensor.create((Object) Boolean.valueOf(z), Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][][] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }

    public static Tensor<Boolean> create(boolean[][][][][][] zArr) {
        return Tensor.create((Object) zArr, Boolean.class);
    }
}
