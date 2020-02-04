package com.android.camera.exif;

public class Rational {
    private final long mDenominator;
    private final long mNumerator;

    public Rational(long j, long j2) {
        this.mNumerator = j;
        this.mDenominator = j2;
    }

    public Rational(Rational rational) {
        this.mNumerator = rational.mNumerator;
        this.mDenominator = rational.mDenominator;
    }

    public long getNumerator() {
        return this.mNumerator;
    }

    public long getDenominator() {
        return this.mDenominator;
    }

    public double toDouble() {
        return ((double) this.mNumerator) / ((double) this.mDenominator);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rational) {
            Rational rational = (Rational) obj;
            if (this.mNumerator == rational.mNumerator && this.mDenominator == rational.mDenominator) {
                z = true;
            }
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mNumerator);
        sb.append("/");
        sb.append(this.mDenominator);
        return sb.toString();
    }
}
