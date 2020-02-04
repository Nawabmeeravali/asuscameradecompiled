package com.asus.scenedetectlib.AISceneDetectInterface.recognition;

import android.annotation.SuppressLint;
import com.asus.scenedetectlib.AISceneDetectInterface.ColorMap;
import com.asus.scenedetectlib.AISceneDetectInterface.SceneType;
import java.util.Comparator;

public class SceneRecognition extends Recognition {
    public static final Comparator<Recognition> CONFIDENCE_COMPARATOR_INV = new Comparator<Recognition>() {
        /* renamed from: a */
        public int compare(Recognition recognition, Recognition recognition2) {
            if (!(recognition instanceof SceneRecognition) || !(recognition2 instanceof SceneRecognition)) {
                return 0;
            }
            SceneRecognition sceneRecognition = (SceneRecognition) recognition2;
            float f = ((SceneRecognition) recognition).confidence;
            float f2 = sceneRecognition.confidence;
            if (f > f2) {
                return -1;
            }
            return f < f2 ? 1 : 0;
        }
    };
    public float confidence;
    public SceneType sceneType;
    public int value = 0;

    public SceneRecognition(SceneType sceneType2, float f) {
        this.sceneType = sceneType2;
        this.confidence = f;
    }

    public static SceneRecognition fromLabel(String str, float f) {
        SceneRecognition sceneRecognition = new SceneRecognition(toType(str), f);
        if (sceneRecognition.sceneType == SceneType.FLOWER) {
            sceneRecognition.value = ColorMap.getOrdinalFromColorMap(toColorMap(str));
        }
        return sceneRecognition;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.asus.scenedetectlib.AISceneDetectInterface.ColorMap toColorMap(java.lang.String r6) {
        /*
            int r0 = r6.hashCode()
            r1 = 5
            r2 = 4
            r3 = 3
            r4 = 2
            r5 = 1
            switch(r0) {
                case -1962451822: goto L_0x003f;
                case -1960404537: goto L_0x0035;
                case -1930543584: goto L_0x002b;
                case -1687840040: goto L_0x0021;
                case -1673941058: goto L_0x0017;
                case 915848365: goto L_0x000d;
                default: goto L_0x000c;
            }
        L_0x000c:
            goto L_0x0049
        L_0x000d:
            java.lang.String r0 = "flower_red"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r4
            goto L_0x004a
        L_0x0017:
            java.lang.String r0 = "flower_blue"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r2
            goto L_0x004a
        L_0x0021:
            java.lang.String r0 = "flower_yellow"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = 0
            goto L_0x004a
        L_0x002b:
            java.lang.String r0 = "flower_purple"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r3
            goto L_0x004a
        L_0x0035:
            java.lang.String r0 = "flower_others"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r1
            goto L_0x004a
        L_0x003f:
            java.lang.String r0 = "flower_orange"
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0049
            r6 = r5
            goto L_0x004a
        L_0x0049:
            r6 = -1
        L_0x004a:
            if (r6 == 0) goto L_0x0068
            if (r6 == r5) goto L_0x0065
            if (r6 == r4) goto L_0x0062
            if (r6 == r3) goto L_0x005f
            if (r6 == r2) goto L_0x005c
            if (r6 == r1) goto L_0x0059
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.NONE
            return r6
        L_0x0059:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.NONE
            return r6
        L_0x005c:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.BLUE
            return r6
        L_0x005f:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.PURPLE
            return r6
        L_0x0062:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.RED
            return r6
        L_0x0065:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.ORANGE
            return r6
        L_0x0068:
            com.asus.scenedetectlib.AISceneDetectInterface.ColorMap r6 = com.asus.scenedetectlib.AISceneDetectInterface.ColorMap.YELLOW
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.toColorMap(java.lang.String):com.asus.scenedetectlib.AISceneDetectInterface.ColorMap");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.asus.scenedetectlib.AISceneDetectInterface.SceneType toType(java.lang.String r1) {
        /*
            int r0 = r1.hashCode()
            switch(r0) {
                case -1962451822: goto L_0x00c4;
                case -1960404537: goto L_0x00b9;
                case -1930543584: goto L_0x00ae;
                case -1687840040: goto L_0x00a3;
                case -1673941058: goto L_0x0098;
                case -1271629221: goto L_0x008d;
                case -891172202: goto L_0x0083;
                case 98262: goto L_0x0079;
                case 99644: goto L_0x006f;
                case 113953: goto L_0x0065;
                case 3148894: goto L_0x005a;
                case 3535235: goto L_0x004f;
                case 105560318: goto L_0x0043;
                case 106748523: goto L_0x0038;
                case 109757182: goto L_0x002d;
                case 861720859: goto L_0x0021;
                case 915848365: goto L_0x0015;
                case 2067670894: goto L_0x0009;
                default: goto L_0x0007;
            }
        L_0x0007:
            goto L_0x00cf
        L_0x0009:
            java.lang.String r0 = "greenland"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 8
            goto L_0x00d0
        L_0x0015:
            java.lang.String r0 = "flower_red"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 13
            goto L_0x00d0
        L_0x0021:
            java.lang.String r0 = "document"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 10
            goto L_0x00d0
        L_0x002d:
            java.lang.String r0 = "stage"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 4
            goto L_0x00d0
        L_0x0038:
            java.lang.String r0 = "plant"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 7
            goto L_0x00d0
        L_0x0043:
            java.lang.String r0 = "ocean"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 9
            goto L_0x00d0
        L_0x004f:
            java.lang.String r0 = "snow"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 2
            goto L_0x00d0
        L_0x005a:
            java.lang.String r0 = "food"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 3
            goto L_0x00d0
        L_0x0065:
            java.lang.String r0 = "sky"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 0
            goto L_0x00d0
        L_0x006f:
            java.lang.String r0 = "dog"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 6
            goto L_0x00d0
        L_0x0079:
            java.lang.String r0 = "cat"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 5
            goto L_0x00d0
        L_0x0083:
            java.lang.String r0 = "sunset"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 1
            goto L_0x00d0
        L_0x008d:
            java.lang.String r0 = "flower"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 17
            goto L_0x00d0
        L_0x0098:
            java.lang.String r0 = "flower_blue"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 15
            goto L_0x00d0
        L_0x00a3:
            java.lang.String r0 = "flower_yellow"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 11
            goto L_0x00d0
        L_0x00ae:
            java.lang.String r0 = "flower_purple"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 14
            goto L_0x00d0
        L_0x00b9:
            java.lang.String r0 = "flower_others"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 16
            goto L_0x00d0
        L_0x00c4:
            java.lang.String r0 = "flower_orange"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x00cf
            r1 = 12
            goto L_0x00d0
        L_0x00cf:
            r1 = -1
        L_0x00d0:
            switch(r1) {
                case 0: goto L_0x00f7;
                case 1: goto L_0x00f4;
                case 2: goto L_0x00f1;
                case 3: goto L_0x00ee;
                case 4: goto L_0x00eb;
                case 5: goto L_0x00e8;
                case 6: goto L_0x00e5;
                case 7: goto L_0x00e2;
                case 8: goto L_0x00df;
                case 9: goto L_0x00dc;
                case 10: goto L_0x00d9;
                case 11: goto L_0x00d6;
                case 12: goto L_0x00d6;
                case 13: goto L_0x00d6;
                case 14: goto L_0x00d6;
                case 15: goto L_0x00d6;
                case 16: goto L_0x00d6;
                case 17: goto L_0x00d6;
                default: goto L_0x00d3;
            }
        L_0x00d3:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.OTHERS
            return r1
        L_0x00d6:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.FLOWER
            return r1
        L_0x00d9:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.TEXT
            return r1
        L_0x00dc:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.OCEAN
            return r1
        L_0x00df:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.GREEN_LAND
            return r1
        L_0x00e2:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.PLANT
            return r1
        L_0x00e5:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.DOG
            return r1
        L_0x00e8:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.CAT
            return r1
        L_0x00eb:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.STAGE
            return r1
        L_0x00ee:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.FOOD
            return r1
        L_0x00f1:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.SNOW
            return r1
        L_0x00f4:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.SUNSET
            return r1
        L_0x00f7:
            com.asus.scenedetectlib.AISceneDetectInterface.SceneType r1 = com.asus.scenedetectlib.AISceneDetectInterface.SceneType.SKY
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asus.scenedetectlib.AISceneDetectInterface.recognition.SceneRecognition.toType(java.lang.String):com.asus.scenedetectlib.AISceneDetectInterface.SceneType");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof SceneRecognition)) {
            return false;
        }
        SceneRecognition sceneRecognition = (SceneRecognition) obj;
        if (this.sceneType == sceneRecognition.sceneType && this.value == sceneRecognition.value) {
            z = true;
        }
        return z;
    }

    public String getName() {
        if (SceneType.FLOWER != this.sceneType || ColorMap.NONE == ColorMap.getFromOrdinal(this.value)) {
            return this.sceneType.toString();
        }
        return String.format("%s (%s)", new Object[]{this.sceneType, ColorMap.getFromOrdinal(this.value)});
    }

    @SuppressLint({"DefaultLocale"})
    public String toString() {
        return String.format("[%s (%s): %.2f%%]", new Object[]{this.sceneType, ColorMap.getFromOrdinal(this.value), Float.valueOf(this.confidence * 100.0f)});
    }
}
