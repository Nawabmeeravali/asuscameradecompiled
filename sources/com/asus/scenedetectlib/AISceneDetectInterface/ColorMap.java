package com.asus.scenedetectlib.AISceneDetectInterface;

public enum ColorMap {
    NONE,
    RED,
    YELLOW,
    PURPLE,
    ORANGE,
    BLUE;

    public static ColorMap getFromOrdinal(int i) {
        ColorMap[] values;
        for (ColorMap colorMap : values()) {
            if (colorMap.ordinal() == i) {
                return colorMap;
            }
        }
        return NONE;
    }

    public static int getOrdinalFromColorMap(ColorMap colorMap) {
        ColorMap[] values;
        for (ColorMap colorMap2 : values()) {
            if (colorMap2 == colorMap) {
                return colorMap2.ordinal();
            }
        }
        return 0;
    }
}
