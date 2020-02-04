package org.tensorflow;

public interface Operand<T> {
    Output<T> asOutput();
}
