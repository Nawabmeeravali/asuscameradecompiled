package com.asus.scenedetectlib.AISceneDetectInterface;

public enum SceneType {
    OTHERS,
    SKY,
    OCEAN,
    GREEN_LAND,
    SUNSET,
    FOOD,
    SNOW,
    TEXT,
    STAGE,
    DOG,
    CAT,
    PLANT,
    FLOWER,
    GENDER(1000);
    

    /* renamed from: b */
    private int f91b;

    private SceneType(int i) {
        this.f91b = i;
    }

    public int getValue() {
        return this.f91b;
    }
}
