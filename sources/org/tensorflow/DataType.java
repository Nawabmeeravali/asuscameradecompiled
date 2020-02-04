package org.tensorflow;

import java.util.HashMap;
import java.util.Map;
import org.tensorflow.types.UInt8;

public enum DataType {
    FLOAT(1),
    DOUBLE(2),
    INT32(3),
    UINT8(4),
    STRING(7),
    INT64(9),
    BOOL(10);
    
    private static final Map<Class<?>, DataType> typeCodes = null;
    private static final DataType[] values = null;
    private final int value;

    static {
        values = values();
        typeCodes = new HashMap();
        typeCodes.put(Float.class, FLOAT);
        typeCodes.put(Double.class, DOUBLE);
        typeCodes.put(Integer.class, INT32);
        typeCodes.put(UInt8.class, UINT8);
        typeCodes.put(Long.class, INT64);
        typeCodes.put(Boolean.class, BOOL);
        typeCodes.put(String.class, STRING);
    }

    private DataType(int i) {
        this.value = i;
    }

    /* access modifiers changed from: 0000 */
    /* renamed from: c */
    public int mo8304c() {
        return this.value;
    }

    static DataType fromC(int i) {
        DataType[] dataTypeArr;
        for (DataType dataType : values) {
            if (dataType.value == i) {
                return dataType;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("DataType ");
        sb.append(i);
        sb.append(" is not recognized in Java (version ");
        sb.append(TensorFlow.version());
        sb.append(")");
        throw new IllegalArgumentException(sb.toString());
    }

    public static DataType fromClass(Class<?> cls) {
        DataType dataType = (DataType) typeCodes.get(cls);
        if (dataType != null) {
            return dataType;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cls.getName());
        sb.append(" objects cannot be used as elements in a TensorFlow Tensor");
        throw new IllegalArgumentException(sb.toString());
    }
}
