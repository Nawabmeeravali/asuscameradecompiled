package org.codeaurora.snapcam.wrapper;

import android.hardware.Camera.Face;
import android.util.Log;
import java.lang.reflect.Method;

public class ExtendedFaceWrapper extends Wrapper {
    private static final String CLASS_NAME = "com.qualcomm.qti.camera.ExtendedFace";
    private static Class<?> mExtendFaceClass;
    private static Method method_getBlinkDetected;
    private static Method method_getFaceRecognized;
    private static Method method_getGazeAngle;
    private static Method method_getLeftEyeBlinkDegree;
    private static Method method_getLeftRightDirection;
    private static Method method_getLeftRightGazeDegree;
    private static Method method_getRightEyeBlinkDegree;
    private static Method method_getRollDirection;
    private static Method method_getSmileDegree;
    private static Method method_getSmileScore;
    private static Method method_getTopBottomGazeDegree;
    private static Method method_getUpDownDirection;

    public static boolean isExtendedFaceInstance(Object obj) {
        if (mExtendFaceClass == null) {
            try {
                mExtendFaceClass = Class.forName(CLASS_NAME);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return mExtendFaceClass.isInstance(obj);
    }

    public static int getSmileDegree(Face face) {
        try {
            if (method_getSmileDegree == null) {
                method_getSmileDegree = getMethod("getSmileDegree");
            }
            return ((Integer) method_getSmileDegree.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getSmileScore(Face face) {
        try {
            if (method_getSmileScore == null) {
                method_getSmileScore = getMethod("getSmileScore");
            }
            return ((Integer) method_getSmileScore.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getBlinkDetected(Face face) {
        try {
            if (method_getBlinkDetected == null) {
                method_getBlinkDetected = getMethod("getBlinkDetected");
            }
            return ((Integer) method_getBlinkDetected.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getFaceRecognized(Face face) {
        try {
            if (method_getFaceRecognized == null) {
                method_getFaceRecognized = getMethod("getFaceRecognized");
            }
            return ((Integer) method_getFaceRecognized.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getGazeAngle(Face face) {
        try {
            if (method_getGazeAngle == null) {
                method_getGazeAngle = getMethod("getGazeAngle");
            }
            return ((Integer) method_getGazeAngle.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getUpDownDirection(Face face) {
        try {
            if (method_getUpDownDirection == null) {
                method_getUpDownDirection = getMethod("getUpDownDirection");
            }
            return ((Integer) method_getUpDownDirection.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getLeftRightDirection(Face face) {
        try {
            if (method_getLeftRightDirection == null) {
                method_getLeftRightDirection = getMethod("getLeftRightDirection");
            }
            return ((Integer) method_getLeftRightDirection.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getRollDirection(Face face) {
        try {
            if (method_getRollDirection == null) {
                method_getRollDirection = getMethod("getRollDirection");
            }
            return ((Integer) method_getRollDirection.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getLeftEyeBlinkDegree(Face face) {
        try {
            if (method_getLeftEyeBlinkDegree == null) {
                method_getLeftEyeBlinkDegree = getMethod("getLeftEyeBlinkDegree");
            }
            return ((Integer) method_getLeftEyeBlinkDegree.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getRightEyeBlinkDegree(Face face) {
        try {
            if (method_getRightEyeBlinkDegree == null) {
                method_getRightEyeBlinkDegree = getMethod("getRightEyeBlinkDegree");
            }
            return ((Integer) method_getRightEyeBlinkDegree.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getLeftRightGazeDegree(Face face) {
        try {
            if (method_getLeftRightGazeDegree == null) {
                method_getLeftRightGazeDegree = getMethod("getLeftRightGazeDegree");
            }
            return ((Integer) method_getLeftRightGazeDegree.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getTopBottomGazeDegree(Face face) {
        try {
            if (method_getTopBottomGazeDegree == null) {
                method_getTopBottomGazeDegree = getMethod("getTopBottomGazeDegree");
            }
            return ((Integer) method_getTopBottomGazeDegree.invoke(face, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static Method getMethod(String str) throws Exception {
        if (Wrapper.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Debug:com.qualcomm.qti.camera.ExtendedFace no ");
            sb.append(str);
            Log.e("Wrapper", sb.toString());
            return null;
        }
        if (mExtendFaceClass == null) {
            mExtendFaceClass = Class.forName(CLASS_NAME);
        }
        return mExtendFaceClass.getDeclaredMethod(str, new Class[0]);
    }
}
