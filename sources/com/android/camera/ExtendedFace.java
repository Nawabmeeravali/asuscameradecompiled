package com.android.camera;

public class ExtendedFace {
    private int mBlinkDetected = 0;
    private int mGazeAngle = 0;
    private int mGazeDirection = 0;
    private int mId;
    private int mLeftrightGaze = 0;
    private int mLeyeBlink = 0;
    private int mReyeBlink = 0;
    private int mRollDirection = 0;
    private int mSmileConfidence = 0;
    private int mSmileDegree = 0;
    private int mTopbottomGaze = 0;

    public ExtendedFace(int i) {
        this.mId = i;
    }

    public int getBlinkDetected() {
        return this.mBlinkDetected;
    }

    public int getLeyeBlink() {
        return this.mLeyeBlink;
    }

    public int getReyeBlink() {
        return this.mReyeBlink;
    }

    public int getSmileDegree() {
        return this.mSmileDegree;
    }

    public int getSmileConfidence() {
        return this.mSmileConfidence;
    }

    public int getLeftrightGaze() {
        return this.mLeftrightGaze;
    }

    public int getTopbottomGaze() {
        return this.mTopbottomGaze;
    }

    public int getGazeDirection() {
        return this.mGazeDirection;
    }

    public int getRollDirection() {
        return this.mRollDirection;
    }

    public void setBlinkDetected(int i) {
        this.mBlinkDetected = i;
    }

    public void setBlinkDegree(byte b, byte b2) {
        this.mLeyeBlink = b;
        this.mReyeBlink = b2;
    }

    public void setSmileDegree(byte b) {
        this.mSmileDegree = b;
    }

    public void setGazeDirection(int i, int i2, int i3) {
        this.mTopbottomGaze = i;
        this.mLeftrightGaze = i2;
        this.mRollDirection = i3;
    }

    public void setGazeAngle(byte b) {
        this.mGazeAngle = b;
    }

    public void setSmileConfidence(int i) {
        this.mSmileConfidence = i;
    }
}
